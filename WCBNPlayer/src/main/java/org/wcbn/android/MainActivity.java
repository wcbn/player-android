package org.wcbn.android;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ShareActionProvider;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.StreamService.StreamBinder;
import org.wcbn.android.station.Station;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener,
        StreamService.OnStateUpdateListener {
    private ShareActionProvider mShareActionProvider;
    private StreamService mService;
    private boolean mBound;
    private final List<UiFragment> mFragments = new ArrayList<UiFragment>();
    private PlaybackFragment mPlaybackFragment = new PlaybackFragment();
    private SongInfoFragment mSongInfoFragment = new SongInfoFragment();
    private Activity mActivity = this;
    private String mShareString;
    private Station mStation;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    public MainActivity() {
        super();
        mStation = Utils.getStation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        actionBar.setListNavigationCallbacks(
                new ArrayAdapter<String>(
                        getActionBarThemedContextCompat(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        getResources().getStringArray(mStation.getTabNames())),
                this);

        if(mFragments.isEmpty())
        for(Class<? extends UiFragment> cls : mStation.getUiFragments()) {
            try {
                mFragments.add(cls.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.playback, mPlaybackFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.info, mSongInfoFragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, StreamService.class);
        bindService(intent, mConnection, Context.BIND_IMPORTANT);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBound)
            mService.setMetadataRefresh(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBound)
            mService.setMetadataRefresh(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getActionBar().getThemedContext();
        } else {
            return this;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
        if(savedInstanceState.containsKey("share_string")) {
            mShareString = savedInstanceState.getString("share_string");
            mShareIntentSet = true;
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
        if(mShareString != null)
            outState.putString("share_string", mShareString);
        super.onSaveInstanceState(outState);
    }

    private boolean mShareIntentSet = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        if(mShareActionProvider != null && !mShareIntentSet) {
            Intent intent = new Intent();
            intent.setType("text/plain");
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
            if(mBound && mService.getStream() != null) {
                mShareString = String.format(
                        getString(R.string.share_string),
                        Utils.capitalizeTitle(mService.getStream().getCurrentSong()),
                        (mService.getStream()).getProgram());
                intent.putExtra(Intent.EXTRA_TEXT, mShareString);
                mShareActionProvider.setShareIntent(intent);
            }
            else {
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_string_default));
                mShareActionProvider.setShareIntent(intent);
            }
            mShareIntentSet = true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        UiFragment fragment = mFragments.get(position);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, (Fragment) fragment)
                .commit();
        return true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            StreamBinder binder = (StreamBinder) service;
            mService = binder.getService();
            mService.setOnStateUpdateListener((StreamService.OnStateUpdateListener) mActivity);
            mBound = true;

            if(mService.isPreparing()) {
                setProgressBarIndeterminateVisibility(true);
            }
            else {
                setProgressBarIndeterminateVisibility(false);
            }

            mPlaybackFragment.setService(mService);
            mSongInfoFragment.setService(mService);

            for(UiFragment fragment : mFragments) {
                fragment.setService(mService);
            }

            mService.setMetadataRefresh(true);

            if(mShareActionProvider != null && mService.getStream() != null) {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(Intent.ACTION_SEND);
                mShareString = String.format(
                        getString(R.string.share_string),
                        Utils.capitalizeTitle(mService.getStream().getCurrentSong()),
                        (mService.getStream()).getProgram());
                intent.putExtra(Intent.EXTRA_TEXT, mShareString);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
                mShareActionProvider.setShareIntent(intent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService.setOnStateUpdateListener(null);
            mBound = false;
            mService.setMetadataRefresh(false);
        }
    };

    @Override
    public void onMediaError(MediaPlayer mp, int what, int extra) {
        setProgressBarIndeterminateVisibility(false);
        mPlaybackFragment.handleMediaError(mp, what, extra);
        mSongInfoFragment.handleMediaError(mp, what, extra);
        for(UiFragment f : mFragments) {
            f.handleMediaError(mp, what, extra);
        }
    }

    @Override
    public void onMediaPlay() {
        setProgressBarIndeterminateVisibility(false);
        mPlaybackFragment.handleMediaPlay();
        mSongInfoFragment.handleMediaPlay();
        for(UiFragment f : mFragments) {
            f.handleMediaPlay();
        }
    }

    @Override
    public void onMediaPause() {
        setProgressBarIndeterminateVisibility(false);
        mPlaybackFragment.handleMediaPause();
        mSongInfoFragment.handleMediaPause();
        for(UiFragment f : mFragments) {
            f.handleMediaPause();
        }
    }

    @Override
    public void onMediaStop() {
        setProgressBarIndeterminateVisibility(false);
        mPlaybackFragment.handleMediaStop();
        mSongInfoFragment.handleMediaStop();
        for(UiFragment f : mFragments) {
            f.handleMediaStop();
        }
    }

    @Override
    public void updateTrack(Stream stream, Station station, Bitmap albumArt) {
        mPlaybackFragment.handleUpdateTrack(stream, station, albumArt);
        mSongInfoFragment.handleUpdateTrack(stream, station, albumArt);

        for(UiFragment f : mFragments) {
            f.handleUpdateTrack(stream, station, albumArt);
        }

        if(mShareActionProvider != null && mBound) {
            Intent intent = new Intent();
            intent.setType("text/plain");
            intent.setAction(Intent.ACTION_SEND);
            mShareString = String.format(
                    getString(R.string.share_string),
                    Utils.capitalizeTitle(stream.getCurrentSong()),
                    ((StreamExt) stream).getProgram());
            intent.putExtra(Intent.EXTRA_TEXT, mShareString);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
            mShareActionProvider.setShareIntent(intent);
        }
    }
}
