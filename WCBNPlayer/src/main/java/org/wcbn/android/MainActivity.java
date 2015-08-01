package org.wcbn.android;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.Context;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.StreamService.StreamBinder;
import org.wcbn.android.station.Station;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActionBar.OnNavigationListener,
        StreamService.OnStateUpdateListener {
    private StreamService mService;
    private boolean mBound, mIsManualOpen = false;
    private final List<UiFragment> mFragments = new ArrayList<UiFragment>();
    private PlaybackFragment mPlaybackFragment = new PlaybackFragment();
    private SongInfoFragment mSongInfoFragment = new SongInfoFragment();
    private Activity mActivity = this;
    private String mShareString;
    private CharSequence mTitle, mDrawerTitle;
    private static final Station sStation;
    private ListView mDrawerList;
    private String[] mTabNames;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurItem;
    private SharedPreferences mPrefs;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    static {
        sStation = Utils.getStation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mTabNames = getResources().getStringArray(sStation.getTabNames());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mTabNames));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = mDrawerTitle = getString(R.string.wcbn);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                if(mIsManualOpen) {
                    SharedPreferences.Editor prefEditor = mPrefs.edit();
                    prefEditor.putBoolean("drawer_opened", true);
                    prefEditor.apply();
                }
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(mFragments.isEmpty()) {
            for(Class<? extends UiFragment> cls : sStation.getUiFragments()) {
                try {
                        mFragments.add(cls.newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
            }
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.playback, mPlaybackFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.info, mSongInfoFragment)
                .commit();

        if(savedInstanceState == null ||
                !savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            mCurItem = 0;
        }
        else {
            mCurItem = savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        if(mBound) {
            // Create a new fragment and specify the planet to show based on position
            UiFragment fragment = mFragments.get(position);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, (Fragment) fragment)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mTabNames[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }

        mCurItem = position;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
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

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey("share_string")) {
            mShareString = savedInstanceState.getString("share_string");
            mShareIntentSet = true;
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                mCurItem);
        if(mShareString != null)
            outState.putString("share_string", mShareString);
        super.onSaveInstanceState(outState);
    }

    private boolean mShareIntentSet = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_share:
                if(mBound && mService.getStream() != null)
                    startActivity(getShareIntent(mService.getStream()));
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

            selectItem(mCurItem);

            mService.setMetadataRefresh(true);

            if(!mPrefs.getBoolean("drawer_opened", false)) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }

            mIsManualOpen = true;
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
    }

    private Intent getShareIntent(Stream stream) {
        Intent intent = new Intent();
        intent.setType("text/plain");
        intent.setAction(Intent.ACTION_SEND);
        mShareString = String.format(
                getString(R.string.share_string),
                Utils.capitalizeTitle(stream.getCurrentSong()),
                ((StreamExt) stream).getProgram());
        intent.putExtra(Intent.EXTRA_TEXT, mShareString);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
        return intent;
    }
}
