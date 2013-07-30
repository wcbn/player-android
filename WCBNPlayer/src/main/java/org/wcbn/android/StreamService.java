package org.wcbn.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

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
    private final IBinder mBinder = new StreamBinder();

    public class StreamBinder extends Binder {
        StreamService getService() {
            return StreamService.this;
        }
    }

    public boolean prepare(MediaPlayer.OnPreparedListener listener) {
        try {
            startForeground(1, mNotificationBuilder.build());
            mPlayer.setOnPreparedListener(listener);
            mPlayer.prepareAsync();
            return true;
        } catch(IllegalStateException e) {
            return false;
        }
    }

    public void startPlayback() {
        mPlayer.start();
    }

    public void stopPlayback() {
        mPlayer.stop();
        stopForeground(true);
    }

    public void pausePlayback() {
        mPlayer.pause();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying() || mPlayer.isLooping();
    }

    @Override
    public IBinder onBind(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String quality = prefs.getString("quality", Quality.HI);

        mStreamUri = Quality.getUri(quality, getResources());

        try {
            mPlayer.setDataSource(this, Uri.parse(mStreamUri));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis());

       return mBinder;
    }
}
