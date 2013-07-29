package org.wcbn.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.io.IOException;

/**
 * Created by mike on 7/29/13.
 */
public class StreamService extends Service {
    public static class Quality {
        public static final int MID = 0;
        public static final int HI = 1;
        public static final int HD = 2;
    }

    private SharedPreferences mPrefs;
    private String mStreamUri;
    private MediaPlayer mPlayer;
    private final IBinder mBinder = new StreamBinder();

    public class StreamBinder extends Binder {
        StreamService getService() {
            return StreamService.this;
        }
    }

    public void startPlayback() {

    }

    public void stopPlayback() {

    }

    public MediaPlayer getMediaPlayer() {
        return mPlayer;
    }

    @Override
    public void onCreate() {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        switch(mPrefs.getInt("quality", Quality.HI)) {
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

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(this, Uri.parse(mStreamUri));
        } catch (IOException e) {
            e.printStackTrace();
        }

       mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
           @Override
           public void onPrepared(MediaPlayer mp) {
               mp.start();
           }
       });

       mPlayer.prepareAsync();
    }

    @Override
    public IBinder onBind(Intent intent) {
       return mBinder;
    }
}
