package org.wcbn.android;


import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import net.moraleboost.streamscraper.Stream;

public class WCBNScheduleFragment extends Fragment implements UiFragment {

    // TODO: Parse the entire table. We're hardcoding three entires for now.

    public static final String SCHEDULE_URI = "http://wcbn.org/schedule";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void setService(Service service) {

    }

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
}
