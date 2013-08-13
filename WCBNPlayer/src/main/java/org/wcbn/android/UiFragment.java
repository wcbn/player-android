package org.wcbn.android;

import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;

import net.moraleboost.streamscraper.Stream;

public interface UiFragment {
    public void handleMediaError(MediaPlayer mp, int what, int extra);
    public void handleMediaPlay();
    public void handleMediaPause();
    public void handleMediaStop();
    public void handleUpdateTrack(Stream stream, Bitmap albumArt);
    public void setService(Service service);
}
