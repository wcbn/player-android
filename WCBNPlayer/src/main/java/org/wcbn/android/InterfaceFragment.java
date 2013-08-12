package org.wcbn.android;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;

import net.moraleboost.streamscraper.Stream;

public interface InterfaceFragment {
    public void handleMediaError(MediaPlayer mp, int what, int extra);
    public void handleMediaPlay();
    public void handleMediaPause();
    public void handleMediaStop();
    public void handleUpdateTrack(Stream stream, Bitmap albumArt);
}
