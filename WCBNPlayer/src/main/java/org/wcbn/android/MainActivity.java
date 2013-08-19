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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
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
    private CharSequence mTitle, mDrawerTitle;
    private Station mStation;
    private ListView mDrawerList;
    private String[] mTabNames;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurItem;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    public MainActivity() {
        super();
        mStation = Utils.getStation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main_drawer);

        final ActionBar actionBar = getActionBar();

        mTabNames = getResources().getStringArray(mStation.getTabNames());
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
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if(mFragments.isEmpty()) {

            Log.d("WCBNPlayer", "Adding fragments…");

            for(Class<? extends UiFragment> cls : mStation.getUiFragments()) {
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
        getActionBar().setTitle(mTitle);
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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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

            Log.d("WCBNPlayer", "Service connected!");

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
