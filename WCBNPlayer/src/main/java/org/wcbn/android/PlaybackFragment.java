package org.wcbn.android;

import android.app.Service;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaybackFragment extends Fragment {

    // Bound StreamService
    private StreamService mService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_playback, null);
        return layout;
    }

    public void setService(Service streamService) {
        mService = (StreamService) streamService;
    }

    public void startPlayback() {
        if(!mService.prepare(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mService.startPlayback();
            }
        })) {
            // MediaPlayer is already prepared, so just start playback
            mService.startPlayback();
        }
    }

    public void pausePlayback() {
        mService.pausePlayback();
    }

    public void stopPlayback() {
        mService.stopPlayback();
    }
}
