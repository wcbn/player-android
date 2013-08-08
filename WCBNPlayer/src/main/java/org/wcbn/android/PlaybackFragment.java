package org.wcbn.android;

import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import net.moraleboost.streamscraper.Stream;

public class PlaybackFragment extends Fragment implements StreamService.OnStateUpdateListener {

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

    @Override
    public void onMediaError(MediaPlayer mp, int what, int extra) {
        updateButtons();
        setProgressBar(false);
        mService.reset();
    }

    @Override
    public void onMediaPlay() {
        updateButtons();
        setProgressBar(false);
    }

    @Override
    public void onMediaPause() {
        updateButtons();
        setProgressBar(false);
    }

    @Override
    public void onMediaStop() {
        updateButtons();
        setProgressBar(false);
    }

    @Override
    public void updateTrack(Stream stream, Bitmap mAlbumArt) {
        
    }

    private class ClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.equals(mButtonPlayPause)) {
                if(!mService.isPlaying()) {
                    startPlayback();
                }
                else {
                    pausePlayback();
                }
            }
            else if(v.equals(mButtonStop)) {
                stopPlayback();
            }
            updateButtons();
        }
    }

    public void setService(Service streamService) {
        mService = (StreamService) streamService;
        mService.setOnStateUpdateListener(this);
        updateButtons();
    }

    public void startPlayback() {
        if(mService != null) {
            setProgressBar(true);
            if(!mService.prepare()) {
                mService.startPlayback();
            }
        }
    }

    public void pausePlayback() {
        if(mService != null) {
            mService.pausePlayback();
        }
    }

    public void stopPlayback() {
        setProgressBar(false);
        if(mService != null) {
            mService.stopPlayback();
        }
    }

    public void setProgressBar(boolean display) {
        if(getActivity() != null) {
            getActivity().setProgressBarIndeterminateVisibility(display);
        }
    }

    public void updateButtons() {
        if(mService.isPlaying()) {
            mButtonPlayPause.setImageResource(R.drawable.btn_playback_pause);
        }
        else {
            mButtonPlayPause.setImageResource(R.drawable.btn_playback_play);
        }
        mButtonStop.setImageResource(R.drawable.btn_playback_stop);
    }
}
