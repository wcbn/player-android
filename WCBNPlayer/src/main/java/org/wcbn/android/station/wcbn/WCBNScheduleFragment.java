package org.wcbn.android.station.wcbn;

import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.moraleboost.streamscraper.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.wcbn.android.station.Station;
import org.wcbn.android.UiFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WCBNScheduleFragment extends Fragment implements UiFragment {

    // TODO: Parse the entire table (from the Google Calendar?).
    // We're hardcoding three entires for now.

    public static final String FRAGMENT_TAG = "WCBNScheduleFragment";

    public static final int NUM_ENTRIES = 3;
    public static final String TAG = "WCBNScheduleFragment";
    public static final String SCHEDULE_URI = "http://wcbn.org/schedule";
    private List<WCBNScheduleItem> mItems;

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

        if(savedInstanceState == null || !savedInstanceState.containsKey("items")) {
            mItems = new ArrayList<WCBNScheduleItem>();
            for(int i = 0; i < NUM_ENTRIES; i++) {
                mItems.add(new WCBNScheduleItem(inflater.getContext()));
            }
        }
        else {
            mItems = savedInstanceState.getParcelableArrayList("items");
            for(WCBNScheduleItem item : mItems) {
                item.initViews(inflater.getContext());
                item.updateViews();
            }
        }

        new ScheduleUpdateTask().execute(SCHEDULE_URI);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity= Gravity.CENTER;

        LinearLayout view = new LinearLayout(inflater.getContext());
        view.setGravity(Gravity.CENTER);
        view.setLayoutParams(params);
        view.setOrientation(LinearLayout.VERTICAL);

        for(WCBNScheduleItem item : mItems) {
            view.addView(item.getView());
        }

        mItems.get(mItems.size()-1).setLast(true);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("items", (ArrayList<? extends Parcelable>) mItems);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setService(Service service) {
        // Nothing
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
                }
            }
        }
    }
}
