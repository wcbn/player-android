package org.wcbn.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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
    private final MediaPlayer mPlayer = new MediaPlayer();
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

    public class StreamBinder extends Binder {
        StreamService getService() {
            return StreamService.this;
        }
    }

    public void reset() {
        stopForeground(true);
        mPlayer.reset();
        initPlayer();
    }

    public boolean prepare() {
        try {
            startForeground(1, mNotificationHelper.getNotification());
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    startPlayback();
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if(mUpdateListener != null)
                        mUpdateListener.onMediaError(mp, what, extra);
                    Log.d("WCBN", "ERROR: "+what+" "+extra);
                    return true;
                }
            });
            mMetadataHandler.post(mMetadataRunnable);
            mPlayer.prepareAsync();
            return true;
        } catch(IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void startPlayback() {
        mPlayer.start();
        if(mUpdateListener != null)
            mUpdateListener.onMediaPlay();
    }

    public void stopPlayback() {
        mPlayer.stop();
        if(mUpdateListener != null)
            mUpdateListener.onMediaStop();
        stopForeground(true);
    }

    public void pausePlayback() {
        mPlayer.pause();
        if(mUpdateListener != null)
            mUpdateListener.onMediaPause();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying() || mPlayer.isLooping();
    }

    public void initPlayer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String quality = prefs.getString("quality", Quality.HI);
        mGrabAlbumArt = prefs.getBoolean("grabAlbumArt", true);

        mStreamUri = Quality.getUri(quality, getResources());
        Log.d("WCBN", "Using URI: "+mStreamUri);

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
        mPlayer.release();
        stopForeground(true);
    }

    private class NotificationHelper {

        private NotificationCompat.Builder mBuilder;
        private String mDj, mCurrentSong, mArtist, mProgram;
        private Bitmap mIcon;


        NotificationHelper() {
            mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setOngoing(true)
                .setWhen(0)
                .setSmallIcon(R.drawable.ic_launcher);
        }

        private void updateBuilder() {
            StringBuilder subTextBuilder = new StringBuilder();
            subTextBuilder.append(getString(R.string.on))
                    .append(" ")
                    .append(mProgram);

            if(mDj != null) {
                subTextBuilder.append(" ")
                    .append(getString(R.string.with))
                    .append(" ")
                    .append(mDj);
            }
            mBuilder.setLargeIcon(mIcon);
            mBuilder.setContentTitle(mCurrentSong);
            mBuilder.setContentText(mArtist);
            mBuilder.setSubText(subTextBuilder.toString());
        }

        public void setBitmap(Bitmap icon) {
            mIcon = icon;
        }

        public void setDj(String dj) {
            mDj = Utils.capitalizeTitle(dj);
        }

        public String getDj() {
            return mDj;
        }

        public void setCurrentSong(String currentSong) {
            mCurrentSong = Utils.capitalizeTitle(currentSong);
        }

        public String getCurrentSong() {
            return mCurrentSong;
        }

        public void setArtist(String artist) {
            mArtist = Utils.capitalizeTitle(artist);
        }

        public String getArtist() {
            return mArtist;
        }

        public void setProgram(String program) {
            mProgram = program;
        }

        public String getProgram() {
            return Utils.capitalizeTitle(mProgram);
        }

        public Notification getNotification() {
            updateBuilder();
            return mBuilder.build();
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
                        ItunesScraper scraper = new ItunesScraper(stream.getCurrentSong());
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
                mNotificationHelper.setCurrentSong(result.getCurrentSong());
                mNotificationHelper.setDj(((StreamExt) result).getDj());
                mNotificationHelper.setArtist(((StreamExt) result).getArtist());
                mNotificationHelper.setProgram(((StreamExt) result).getProgram());

                mNotificationManager.notify(1, mNotificationHelper.getNotification());

                if(mUpdateListener != null)
                    mUpdateListener.updateTrack(result, mLargeAlbumArt);
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

    public interface OnStateUpdateListener {
        public void onMediaError(MediaPlayer mp, int what, int extra);
        public void onMediaPlay();
        public void onMediaPause();
        public void onMediaStop();
        public void updateTrack(Stream stream, Bitmap albumArt);
    }
}
