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


public class NowPlayingFragment extends Fragment implements UiFragment {

    private ImageView mAlbumArtView;
    private Bitmap mAlbumArtBitmap;
    private StreamService mService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nowplaying, null);
        mAlbumArtView = (ImageView) view.findViewById(R.id.album_art);

        if(savedInstanceState != null && savedInstanceState.containsKey("bitmap")) {
            mAlbumArtBitmap = (Bitmap) savedInstanceState.get("bitmap");
            mAlbumArtView.setImageBitmap(mAlbumArtBitmap);
        }

        return view;
    }

    @Override
    public void setService(Service service) {
        mService = (StreamService) service;

        mAlbumArtBitmap = mService.getAlbumArt();
        if(mAlbumArtBitmap != null) {
            mAlbumArtView.setImageBitmap(mAlbumArtBitmap);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(mAlbumArtBitmap != null)
            savedInstanceState.putParcelable("bitmap", mAlbumArtBitmap);
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
    public void handleUpdateTrack(Stream stream, Bitmap albumArt) {
        if(albumArt != null) {
            mAlbumArtView.setImageBitmap(albumArt);
            mAlbumArtBitmap = albumArt;
        }
    }
}
