package org.wcbn.android;

import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.station.Station;

/**
 * Contains helpful methods, mostly for the playback Service to update the state of the app UI.
 *
 * All Fragments in the UI should implement this interface.
 */
public interface UiFragment {
    public void handleMediaError(MediaPlayer mp, int what, int extra);
    public void handleMediaPlay();
    public void handleMediaPause();
    public void handleMediaStop();
    public void handleUpdateTrack(Stream stream, Station station, Bitmap albumArt);
    public void setService(Service service);
}
