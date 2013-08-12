package org.wcbn.android;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.StreamService.StreamBinder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener,
        StreamService.OnStateUpdateListener {
    private StreamService mService;
    private boolean mBound;
    private final List<InterfaceFragment> mFragments = new ArrayList<InterfaceFragment>();
    private NowPlayingFragment mNowPlayingFragment = new NowPlayingFragment();
    private ScheduleFragment mScheduleFragment = new ScheduleFragment();
    private PlaybackFragment mPlaybackFragment = new PlaybackFragment();
    private Activity mActivity = this;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        actionBar.setListNavigationCallbacks(
                new ArrayAdapter<String>(
                        getActionBarThemedContextCompat(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[] {
                                getString(R.string.title_now_playing),
                                getString(R.string.title_schedule)
                        }),
                this);

        mFragments.add(mNowPlayingFragment);
        mFragments.add(mScheduleFragment);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.playback, mPlaybackFragment)
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        InterfaceFragment fragment = mFragments.get(position);
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
            mBound = true;

            mPlaybackFragment.setService(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onMediaError(MediaPlayer mp, int what, int extra) {
        for(InterfaceFragment f : mFragments) {
            f.handleMediaError(mp, what, extra);
        }
    }

    @Override
    public void onMediaPlay() {
        for(InterfaceFragment f : mFragments) {
            f.handleMediaPlay();
        }
    }

    @Override
    public void onMediaPause() {
        for(InterfaceFragment f : mFragments) {
            f.handleMediaPause();
        }
    }

    @Override
    public void onMediaStop() {
        for(InterfaceFragment f : mFragments) {
            f.handleMediaStop();
        }
    }

    @Override
    public void updateTrack(Stream stream, Bitmap albumArt) {
        for(InterfaceFragment f : mFragments) {
            f.handleUpdateTrack(stream, albumArt);
        }
    }
}
