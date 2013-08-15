package org.wcbn.android.station.wcbn;


import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.moraleboost.streamscraper.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wcbn.android.R;
import org.wcbn.android.Station;
import org.wcbn.android.UiFragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WCBNScheduleFragment extends Fragment implements UiFragment {

    // TODO: Parse the entire table (from the Google Calendar?).
    // We're hardcoding three entires for now.

    public static final int NUM_ENTRIES = 3;
    public static final String TAG = "WCBNScheduleFragment";
    public static final String SCHEDULE_URI = "http://wcbn.org/schedule";
    private List<ScheduleItem> mItems;
    private LayoutInflater mInflater;
    private Typeface mTypeface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;

        mTypeface = Typeface.createFromAsset(inflater.getContext().getAssets(), "Roboto-Light.ttf");

        mItems = new ArrayList<ScheduleItem>();
        for(int i = 0; i < NUM_ENTRIES; i++) {
            mItems.add(new ScheduleItem());
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

        for(ScheduleItem item : mItems) {
            view.addView(item.getView());
        }

        mItems.get(mItems.size()-1).setLast(true);


        return view;
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

    private class ScheduleItem {
        private String mTime;
        private String mDj;
        private String mProgram;
        private String mUri;
        private ViewGroup mView;

        public ScheduleItem() {
            mView = (ViewGroup) mInflater.inflate(R.layout.item_schedule, null);

            ((TextView) mView.findViewById(R.id.time_text)).setTypeface(mTypeface);
            ((TextView) mView.findViewById(R.id.program_text)).setTypeface(mTypeface);
            ((TextView) mView.findViewById(R.id.dj_text)).setTypeface(mTypeface);
        }

        public void setLast(boolean last) {
            if(last) {
                mView.findViewById(R.id.line_1).setVisibility(View.INVISIBLE);
            }
            else {
                mView.findViewById(R.id.line_1).setVisibility(View.VISIBLE);
            }
        }

        public ViewGroup getView() {
            return mView;
        }

        @Override
        public String toString() {
            return "Program: "+mProgram+
                    " DJ: "+mDj+
                    " URI: "+mUri+
                    " Time: "+mTime;
        }

        public void setElement(Element element) {

            mProgram = null;
            mDj = null;
            mUri = null;
            mTime = null;

            Elements links = element.select("a[href]");
            if(!links.isEmpty()) {
                mUri = links.get(0).attr("href");
            }

            String text = element.text().trim();

            if(text != null) {
                Matcher djMatcher = sDjPattern.matcher(text);

                int timeStart = 0;

                if(djMatcher.find()) {
                    mDj = djMatcher.group(1);
                    Matcher programMatcher = sProgramPattern.matcher(text);
                    if(programMatcher.find()) {
                        mProgram = programMatcher.group(1);
                    }
                    timeStart = djMatcher.end();
                }
                else {
                    Matcher programMatcher = sProgramPatternNoDj.matcher(text);
                    if(programMatcher.find()) {
                        mProgram = programMatcher.group(1);
                    }
                    timeStart = programMatcher.end();
                }

                if(timeStart != 0) {
                    mTime = text.substring(timeStart - 4).trim();
                    String[] times =  mTime.split("-");
                    String startTime = times[0];
                    String endTime = times[1];

                    if(!(startTime.contains("am") || startTime.contains("pm"))) {
                        if(endTime.contains("am")) {
                            startTime = startTime + "am";
                        }
                        else if(endTime.contains("pm")) {
                            startTime = startTime + "pm";
                        }
                    }

                    mTime = startTime + " - " + endTime;
                }
            }

            // Update our views
            if(mDj != null) {
                mView.findViewById(R.id.dj_text).setVisibility(View.VISIBLE);
                ((TextView) mView.findViewById(R.id.dj_text)).setText(mDj);
            }
            else {
                mView.findViewById(R.id.dj_text).setVisibility(View.INVISIBLE);
            }
            ((TextView) mView.findViewById(R.id.program_text)).setText(mProgram);
            ((TextView) mView.findViewById(R.id.time_text)).setText(mTime);
            if(mUri != null) {
                ((ImageView) mView.findViewById(R.id.icon_link))
                        .setImageResource(R.drawable.ic_menu_globe);
                mView.findViewById(R.id.btn_link).setClickable(true);
                mView.findViewById(R.id.btn_link).setFocusable(true);
                mView.findViewById(R.id.btn_link).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent()
                            .setAction(Intent.ACTION_VIEW)
                            .setData(Uri.parse(mUri)));
                    }
                });
            }
            else {
                ((ImageView) mView.findViewById(R.id.icon_link))
                        .setImageResource(R.drawable.ic_menu_globe_disabled);
                mView.findViewById(R.id.btn_link).setClickable(false);
                mView.findViewById(R.id.btn_link).setFocusable(false);
            }
        }

        public String getTime() {
            return mTime;
        }


        public String getDj() {
            return mDj;
        }

        public String getProgram() {
            return mProgram;
        }

        public String getUri() {
            return mUri;
        }

        @Override
        public boolean equals(Object item) {
            if (item == null)
                return false;
            if (item == this)
                return true;
            if (!(item instanceof ScheduleItem))
                return false;

            ScheduleItem other = (ScheduleItem) item;

            if(other.getTime().equals(mTime) &&
                    other.getDj().equals(mDj) &&
                    other.getProgram().equals(mProgram) &&
                    other.getUri().equals(mUri)) {

                return true;
            }
            return false;
        }
    }

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
