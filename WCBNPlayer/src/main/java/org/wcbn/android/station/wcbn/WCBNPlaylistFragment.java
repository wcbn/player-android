package org.wcbn.android.station.wcbn;

import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.station.Station;
import org.wcbn.android.UiFragment;

public class WCBNPlaylistFragment extends Fragment implements UiFragment {

    public static final String PLAYLIST_URI
            = "http://wcbn.org/ryan-playlist/searchplaylist.php?howmany=3&unit=hour";

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
    public void handleUpdateTrack(Stream stream, Station station, Bitmap albumArt) {

    }

    @Override
    public void setService(Service service) {

    }
}
