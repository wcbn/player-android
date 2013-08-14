package org.wcbn.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.scraper.IceCastScraper;

import org.wcbn.android.station.WCBNStation;

/**
 * Android Service that handles background music playback and metadata fetch.
 */
public class StreamService extends Service {

    public static final String TAG = "WCBN StreamService";

    public static final String ACTION_PLAY_PAUSE = "org.wcbn.android.intent.ACTION_PLAY_PAUSE";
    public static final String ACTION_STOP = "org.wcbn.android.intent.ACTION_STOP";

    public static class Quality {
        public static final String MID = "0";
        public static final String HI = "1";
        public static final String HD = "2";

        public static String getUri(String quality, Resources res) {
            return res.getStringArray(R.array.stream_uri)[Integer.parseInt(quality)];
        }
    }

    public static final long DELAY_MS = 10000;

    private String mStreamUri;
    private MediaPlayer mPlayer;
    private final IBinder mBinder = new StreamBinder();
    private OnStateUpdateListener mUpdateListener;
    private Handler mMetadataHandler = new Handler();
    private Runnable mMetadataRunnable = new MetadataUpdateRunnable();
    private NotificationHelper mNotificationHelper;
    private NotificationManager mNotificationManager;
    private boolean mGrabAlbumArt;
    private Scraper mScraper = new IceCastScraper();
    private Station mStation = new WCBNStation();
    private Bitmap mLargeAlbumArt;
    private StreamExt mCurStream;
    private boolean mIsPaused = true;

    public class StreamBinder extends Binder {
        StreamService getService() {
            return StreamService.this;
        }
    }

    public void reset() {
        stopForeground(true);
        mPlayer.release();
        initPlayer();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getAction());

            if(ACTION_PLAY_PAUSE.equals(intent.getAction())) {
                if(mIsPaused) {
                    startPlayback();
                }
                else {
                    pausePlayback();
                }
            }
            else if(ACTION_STOP.equals(intent.getAction())) {
                stopPlayback();
            }
        }
    };

    public boolean prepare() {
        try {
            mNotificationHelper.setPlaying(true);
            mIsPaused = false;
            startForeground(1, mNotificationHelper.getNotification());
            mMetadataHandler.post(mMetadataRunnable);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PLAY_PAUSE);
            filter.addAction(ACTION_STOP);
            registerReceiver(mReceiver, filter);
            if(mPlayer.isPlaying())
                reset();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Check if we're not paused in case the user presses the pause button during
                    // preparation.
                    if(!mIsPaused)
                        startPlayback();
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG, "ERROR: "+what+" "+extra);
                    if(mUpdateListener != null)
                        mUpdateListener.onMediaError(mp, what, extra);
                    return true;
                }
            });
            mPlayer.prepareAsync();
            return true;
        } catch(IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void startPlayback() {
        mPlayer.start();
        mIsPaused = false;
        mNotificationHelper.setPlaying(true);
        mNotificationManager.notify(1, mNotificationHelper.getNotification());
        if(mUpdateListener != null)
            mUpdateListener.onMediaPlay();
    }

    public void stopPlayback() {
        mIsPaused = true;
        if(mUpdateListener != null)
            mUpdateListener.onMediaStop();
        stopForeground(true);

        mMetadataHandler.removeCallbacks(mMetadataRunnable);

        reset();

        try {
            unregisterReceiver(mReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace(); // Already unregistered
        }
    }

    public void pausePlayback() {
        mPlayer.pause();
        mIsPaused = true;
        mNotificationHelper.setPlaying(false);
        mNotificationManager.notify(1, mNotificationHelper.getNotification());
        if(mUpdateListener != null)
            mUpdateListener.onMediaPause();
    }

    public boolean isPlaying() {
        return !mIsPaused;
    }

    public void initPlayer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mPlayer = new MediaPlayer();

        String quality = prefs.getString("quality", Quality.HI);
        mGrabAlbumArt = prefs.getBoolean("grabAlbumArt", true);

        mStreamUri = Quality.getUri(quality, getResources());
        Log.d(TAG, "Using URI: "+mStreamUri);

        try {
            mPlayer.setDataSource(this, Uri.parse(mStreamUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        initPlayer();

        mNotificationHelper = new NotificationHelper();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        return mBinder;
    }

    @Override
    public void onDestroy() {
        if(mPlayer != null)
            mPlayer.release();
        stopForeground(true);
    }

    private class NotificationHelper {

        private NotificationCompat.Builder mBuilderPlaying, mBuilderPaused;
        private NotificationCompat.Builder mCurrentBuilder;
        private String mTitle, mText, mSubText;
        private Bitmap mIcon;
        private PendingIntent mPlayPauseIntent, mStopIntent;

        NotificationHelper() {
            mBuilderPlaying = new NotificationCompat.Builder(getApplicationContext())
                .setOngoing(true)
                .setWhen(0)
                .setSmallIcon(R.drawable.ic_launcher);
            mBuilderPaused = new NotificationCompat.Builder(getApplicationContext())
                .setOngoing(true)
                .setWhen(0)
                .setSmallIcon(R.drawable.ic_launcher);

            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilderPlaying.setContentIntent(resultPendingIntent);
            mBuilderPaused.setContentIntent(resultPendingIntent);

            Intent playPause = new Intent(ACTION_PLAY_PAUSE);
            Intent stop = new Intent(ACTION_STOP);
            mPlayPauseIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, playPause, 0);
            mStopIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, stop, 0);
            mBuilderPlaying.addAction(R.drawable.btn_playback_pause,
                    getString(R.string.pause),
                    mPlayPauseIntent);
            mBuilderPaused.addAction(R.drawable.btn_playback_play,
                    getString(R.string.play),
                    mPlayPauseIntent);
            mBuilderPlaying.addAction(R.drawable.btn_playback_stop,
                    getString(R.string.stop),
                    mStopIntent);
            mBuilderPaused.addAction(R.drawable.btn_playback_stop,
                    getString(R.string.stop),
                    mStopIntent);

            mCurrentBuilder = mBuilderPlaying;
        }

        private void updateBuilder() {
            mCurrentBuilder.setLargeIcon(mIcon);
            mCurrentBuilder.setContentTitle(mTitle);
            mCurrentBuilder.setContentText(mText);
            mCurrentBuilder.setSubText(mSubText);
        }

        public void setBitmap(Bitmap icon) {
            mIcon = icon;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public void setText(String text) {
            mText = text;
        }

        public void setSubText(String subText) {
            mSubText = subText;
        }

        // Notification action update. Only on JELLY_BEAN and above.
        public void setPlaying(boolean playing) {
            if(playing) {
                mCurrentBuilder = mBuilderPlaying;
            }
            else {
                mCurrentBuilder = mBuilderPaused;
            }
        }

        public Notification getNotification() {
            updateBuilder();
            return mCurrentBuilder.build();
        }

    }

    private class MetadataUpdateRunnable implements Runnable {
        @Override
        public void run() {
            new MetadataUpdateTask().execute();
        }
    }

    private class MetadataUpdateTask extends AsyncTask<Stream, Void, Stream> {

        @Override
        protected Stream doInBackground(Stream... previousStream) {

            try {
                List<Stream> streams = mScraper.scrape(new URI(mStreamUri));
                StreamExt stream = mStation.fixMetadata(streams.get(0));

                // Check if we're on the same song. If not, refresh metadata.
                if(mCurStream == null || !(mCurStream.getCurrentSong()
                        .equals(stream.getCurrentSong()))) {
                    mCurStream = stream;

                    if(mLargeAlbumArt != null)
                        mLargeAlbumArt.recycle();

                    if(mGrabAlbumArt) {
                        ItunesScraper scraper = new ItunesScraper(stream.getCurrentSong() + " " +
                            stream.getArtist());
                        mLargeAlbumArt = scraper.getLargeAlbumArt();
                    }
                    return stream;
                }

                return null;

            } catch(URISyntaxException e) {
                e.printStackTrace();
                return null;
            } catch(ScrapeException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onPostExecute(Stream result) {
            if(result != null) {
                if(mGrabAlbumArt) {
                    mNotificationHelper.setBitmap(mLargeAlbumArt);
                }

                mNotificationHelper.setTitle(mStation.getSongName((StreamExt) result
                        , getApplicationContext()));
                mNotificationHelper.setText(mStation.getArtistName((StreamExt) result
                        , getApplicationContext()));
                mNotificationHelper.setSubText(mStation.getDescription((StreamExt) result
                        , getApplicationContext()));

                if(mIsPaused) {
                    mNotificationHelper.setPlaying(false);
                }
                else {
                    mNotificationHelper.setPlaying(true);
                }

                mNotificationManager.notify(1, mNotificationHelper.getNotification());

                if(mUpdateListener != null)
                    mUpdateListener.updateTrack(result, mStation, mLargeAlbumArt);
            }
            mMetadataHandler.postDelayed(mMetadataRunnable, DELAY_MS);
        }
    }

    public void setOnStateUpdateListener(OnStateUpdateListener listener) {
        mUpdateListener = listener;
    }

    public Bitmap getAlbumArt() {
        return mLargeAlbumArt;
    }

    public Station getStation() {
        return mStation;
    }

    public StreamExt getStream() {
        return mCurStream;
    }

    public interface OnStateUpdateListener {
        public void onMediaError(MediaPlayer mp, int what, int extra);
        public void onMediaPlay();
        public void onMediaPause();
        public void onMediaStop();
        public void updateTrack(Stream stream, Station station, Bitmap albumArt);
    }
}
