package org.wcbn.android;

import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.station.Station;

/**
 * Displays album art.
 */
public class AlbumArtFragment extends Fragment implements UiFragment {

    private ImageView mAlbumArtView;
    private Bitmap mAlbumArtBitmap;
    private StreamService mService;

    public static final String FRAGMENT_TAG = "AlbumArtFragment";

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albumart, null);
        mAlbumArtView = (ImageView) view.findViewById(R.id.album_art);

        if(mAlbumArtBitmap != null) {
            mAlbumArtView.setImageBitmap(mAlbumArtBitmap);
        }

        else if(savedInstanceState != null && savedInstanceState.containsKey("bitmap")) {
            mAlbumArtBitmap = (Bitmap) savedInstanceState.get("bitmap");
            mAlbumArtView.setImageBitmap(mAlbumArtBitmap);
        }

        return view;
    }

    @Override
    public void setService(Service service) {
        mService = (StreamService) service;

        mAlbumArtBitmap = mService.getAlbumArt();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(mAlbumArtBitmap != null)
            savedInstanceState.putParcelable("bitmap", mAlbumArtBitmap);

        super.onSaveInstanceState(savedInstanceState);
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
        if(albumArt != null && isVisible()) {
            mAlbumArtView.setImageBitmap(albumArt);
            mAlbumArtBitmap = albumArt;
        }
    }
}
