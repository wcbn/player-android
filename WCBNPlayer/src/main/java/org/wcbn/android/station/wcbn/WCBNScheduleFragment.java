package org.wcbn.android.station.wcbn;


import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.moraleboost.streamscraper.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.wcbn.android.R;
import org.wcbn.android.StreamService;
import org.wcbn.android.station.Station;
import org.wcbn.android.UiFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WCBNScheduleFragment extends Fragment implements UiFragment {

    // TODO: Parse the entire table (from the Google Calendar?).
    // We're hardcoding three entires for now.
    // Persistence is done pretty badly right now.

    public static final int NUM_ENTRIES = 3;
    public static final String TAG = "WCBNScheduleFragment";
    public static final String SCHEDULE_URI = "http://wcbn.org/schedule";
    private List<WCBNScheduleItem> mItems;
    private StreamService mService;
    private LinearLayout mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity= Gravity.CENTER;

        LinearLayout view = new LinearLayout(inflater.getContext());
        view.setGravity(Gravity.CENTER);
        view.setLayoutParams(params);
        view.setOrientation(LinearLayout.VERTICAL);

        mView = view;

        if(mItems != null)
            for(WCBNScheduleItem item : mItems) {
                ViewGroup parent = (ViewGroup) item.getView().getParent();
                if(parent != null)
                    parent.removeView(item.getView());
                mView.addView(item.getView());
            }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new ScheduleUpdateTask().execute(SCHEDULE_URI);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.schedule, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_refresh:
                for(int i = 0; i < NUM_ENTRIES; i++) {
                    mItems.get(i).setLoading(true);
                }
                new ScheduleUpdateTask().execute(SCHEDULE_URI);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mItems != null && !mItems.isEmpty() && mItems.get(0).getProgram() != null) {
            mService.getPersistData().putParcelableArrayList(TAG+".schedule_items",
                    (ArrayList<WCBNScheduleItem>) mItems);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void setService(Service service) {
        mService = (StreamService) service;

        if(mService.getPersistData().containsKey(TAG + ".schedule_items")) {
            mItems = mService.getPersistData().getParcelableArrayList(TAG+".schedule_items");
            mService.getPersistData().remove(TAG+".schedule_items");

            for(WCBNScheduleItem item : mItems) {
                item.initViews(mService.getApplicationContext());
                item.updateViews();
                item.setLoading(false);
            }
        }
        else {
            mItems = new ArrayList<>();
            for(int i = 0; i < NUM_ENTRIES; i++) {
                mItems.add(new WCBNScheduleItem(mService.getApplicationContext()));
                mItems.get(i).setLoading(true);
            }
            new ScheduleUpdateTask().execute(SCHEDULE_URI);
        }

        mItems.get(mItems.size()-1).setLast(true);

        if(mView != null && mView.getChildAt(0) == null) {
            for(WCBNScheduleItem item : mItems) {
                ViewGroup parent = (ViewGroup) item.getView().getParent();
                if(parent != null)
                    parent.removeView(item.getView());
                mView.addView(item.getView());
            }
        }
    }

    @Override
    public void handleMediaError(MediaPlayer mp, int what, int extra) {
        // Nothing
    }

    @Override
    public void handleMediaPlay() {
        // Nothing
    }

    @Override
    public void handleMediaPause() {
        // Nothing
    }

    @Override
    public void handleMediaStop() {
        // Nothing
    }

    @Override
    public void handleUpdateTrack(Stream stream, Station station, Bitmap albumArt) {
        // Nothing
    }

    // TODO: Make the matching algorithm more robust and more able to handle edge cases.

    static final Pattern sDjPattern =
            Pattern.compile(" w/ (.*?) ([0-9]|0[0-9]|1[0-2]):[0-5][0-9]$*");
    static final Pattern sProgramPattern = Pattern.compile("^(.*?) w/");
    static final Pattern sProgramPatternNoDj = Pattern
            .compile("^(.*?) ([0-9]|0[0-9]|1[0-2]):[0-5][0-9]$*");

    private class ScheduleUpdateTask extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... Uris) {
            Document doc;

            try {
                doc = Jsoup.connect(Uris[0])
                        .userAgent("Android")
                        .get();
            } catch(IOException e) {
                Log.d(TAG, "Error downloading schedule");
                e.printStackTrace();
                return null;
            }
            return doc;
        }

        @Override
        public void onPostExecute(Document result) {
            if(result != null) {
                Elements elements = result
                        .select("ul[id=whatsnext]")
                        .select("li");
                for(int i = 0; i < elements.size(); i++) {
                    mItems.get(i).setElement(elements.get(i));
                    mItems.get(i).setLoading(false);
                }
            }
            else {
                for(WCBNScheduleItem item : mItems) {
                    item.setLoading(false);
                }
            }
        }
    }
}
