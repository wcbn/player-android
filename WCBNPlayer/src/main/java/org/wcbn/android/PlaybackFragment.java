package org.wcbn.android;

import android.app.Service;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class PlaybackFragment extends Fragment {

    // Bound StreamService
    private StreamService mService;
    private ClickListener mClickListener = new ClickListener();
    private ImageButton mButtonPlayPause, mButtonStop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_playback, null);

        mButtonPlayPause = (ImageButton) layout.findViewById(R.id.button_play_pause);
        mButtonStop = (ImageButton) layout.findViewById(R.id.button_stop);

        mButtonPlayPause.setOnClickListener(mClickListener);
        mButtonStop.setOnClickListener(mClickListener);

        return layout;
    }

    private class ClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            if(v.equals(mButtonPlayPause)) {
                if(!mService.isPlaying()) {
                    startPlayback();
                    mButtonPlayPause.setImageResource(R.drawable.btn_playback_pause);
                }
                else {
                    pausePlayback();
                    mButtonPlayPause.setImageResource(R.drawable.btn_playback_play);
                }
            }
            else if(v.equals(mButtonStop)) {
                stopPlayback();
                mButtonPlayPause.setImageResource(R.drawable.btn_playback_play);
            }
        }
    }

    public void setService(Service streamService) {
        mService = (StreamService) streamService;
        if(mService.isPlaying()) {
            mButtonPlayPause.setImageResource(R.drawable.btn_playback_pause);
        }
        else {
            mButtonPlayPause.setImageResource(R.drawable.btn_playback_play);
        }
        mButtonStop.setImageResource(R.drawable.btn_playback_repeat);
    }

    public void startPlayback() {
        if(mService != null) {
            getActivity().setProgressBarIndeterminateVisibility(true);
            if(!mService.prepare(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mService.startPlayback();
                getActivity().setProgressBarIndeterminateVisibility(false);
            }
        })) {
                // MediaPlayer is already prepared, so just start playback
                mService.startPlayback();
                getActivity().setProgressBarIndeterminateVisibility(false);
            }
        }
    }

    public void pausePlayback() {
        if(mService != null) {
            mService.pausePlayback();
        }
    }

    public void stopPlayback() {
        getActivity().setProgressBarIndeterminateVisibility(false);
        if(mService != null) {
            mService.stopPlayback();
        }
    }
}
