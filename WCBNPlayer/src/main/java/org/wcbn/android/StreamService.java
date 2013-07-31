package org.wcbn.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
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

public class StreamService extends Service {
    public static class Quality {
        public static final String MID = "0";
        public static final String HI = "1";
        public static final String HD = "2";

        public static String getUri(String quality, Resources res) {
            return res.getStringArray(R.array.stream_uri)[Integer.parseInt(quality)];
        }
    }

    private NotificationCompat.Builder mNotificationBuilder;
    private String mStreamUri;
    private final MediaPlayer mPlayer = new MediaPlayer();
    private final MediaMetadataRetriever mMetadata = new MediaMetadataRetriever();
    private final IBinder mBinder = new StreamBinder();
    private OnStateUpdateListener mUpdateListener;

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
            startForeground(1, mNotificationBuilder.build());
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    startPlayback();
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mUpdateListener.onMediaError(mp, what, extra);
                    Log.d("WCBN", "ERROR: "+what+" "+extra);
                    return true;
                }
            });
            mPlayer.prepareAsync();
            return true;
        } catch(IllegalStateException e) {
            return false;
        }
    }

    public void startPlayback() {
        mPlayer.start();
        mUpdateListener.onMediaPlay();
    }

    public void stopPlayback() {
        mPlayer.stop();
        mUpdateListener.onMediaStop();
        stopForeground(true);
    }

    public void pausePlayback() {
        mPlayer.pause();
        mUpdateListener.onMediaPause();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying() || mPlayer.isLooping();
    }

    public void initPlayer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String quality = prefs.getString("quality", Quality.HI);

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

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis());

        new MetadataUpdateTask().execute();


       return mBinder;
    }

    private class MetadataUpdateTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            Scraper scraper = new IceCastScraper();

            try {
                List<Stream> streams = scraper.scrape(new URI(mStreamUri));

                for (Stream stream: streams) {
                    Log.d("WCBN", "Song Title: " + stream.getCurrentSong());
                    Log.d("WCBN", "URI: " + stream.getUri());
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
    }

    public void setOnStateUpdateListener(OnStateUpdateListener listener) {
        mUpdateListener = listener;
    }

    public interface OnStateUpdateListener {
        public void onMediaError(MediaPlayer mp, int what, int extra);
        public void onMediaPlay();
        public void onMediaPause();
        public void onMediaStop();
        public void updateTrack(Stream stream);
    }
}
