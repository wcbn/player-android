package org.wcbn.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

public class StreamService extends Service {
    public static class Quality {
        public static final int MID = 0;
        public static final int HI = 1;
        public static final int HD = 2;
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
            startForeground(99, mNotificationBuilder.build());
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

    @Override
    public IBinder onBind(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        switch(prefs.getInt("quality", Quality.HI)) {
            case Quality.MID:
                mStreamUri = getResources().getString(R.string.uri_quality_mid);
                break;
            case Quality.HI:
                mStreamUri = getResources().getString(R.string.uri_quality_hi);
                break;
            case Quality.HD:
                mStreamUri = getResources().getString(R.string.uri_quality_hd);
                break;
            default:
                mStreamUri = getResources().getString(R.string.uri_quality_hi);
                break;
        }

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
