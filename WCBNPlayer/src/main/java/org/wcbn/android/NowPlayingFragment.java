package org.wcbn.android;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;

import net.moraleboost.streamscraper.Stream;


public class NowPlayingFragment extends Fragment implements InterfaceFragment {
    public Bitmap mAlbumArt;

    @Override
    public void handleMediaError(MediaPlayer mp, int what, int extra) {

    }

    @Override
    public void handleMediaPlay() {

    }

    @Override
    public void handleMediaPause() {

    }

    @Override
    public void handleMediaStop() {

    }

    @Override
    public void handleUpdateTrack(Stream stream, Bitmap albumArt) {

    }
}