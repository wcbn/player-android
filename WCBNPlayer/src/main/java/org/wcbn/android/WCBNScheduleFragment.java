package org.wcbn.android;


import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.moraleboost.streamscraper.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WCBNScheduleFragment extends Fragment implements UiFragment {

    // TODO: Parse the entire table. We're hardcoding three entires for now.

    public static final String TAG = "WCBNScheduleFragment";
    public static final String SCHEDULE_URI = "http://wcbn.org/schedule";
    private List<ScheduleItem> mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItems = new ArrayList<ScheduleItem>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new ScheduleUpdateTask().execute(SCHEDULE_URI);



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

    // TODO: Make the matching algorithm more robust and more able to handle edge cases.

    static Pattern sDjPattern = Pattern.compile(" w/ (.*?) ([0-9]|0[0-9]|1[0-2]):[0-5][0-9]$*");
    static Pattern sProgramPattern = Pattern.compile("^(.*?) w/");
    static Pattern sProgramPatternNoDj = Pattern
            .compile("^(.*?) ([0-9]|0[0-9]|1[0-2]):[0-5][0-9]$*");
    static SimpleDateFormat sDateFormatter = new SimpleDateFormat("h:mma");

    private class ScheduleItem {
        private String mTime;
        private String mDj;
        private String mProgram;
        private String mUri;

        @Override
        public String toString() {
            return "Program: "+mProgram+
                    " DJ: "+mDj+
                    " URI: "+mUri+
                    " Time: "+mTime;
        }

        public void setElement(Element element) {

            Elements links = element.select("a[href]");
            if(!links.isEmpty()) {
                mUri = links.get(0).attr("href");
            }

            String text = element.text().trim();
            boolean nextDay = false;

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
                    mTime = text.substring(timeStart-4);
                }
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
                for(Element e : elements) {
                    Log.d("WCBN", e.text());
                    ScheduleItem item = new ScheduleItem();
                    item.setElement(e);
                    Log.d("WCBN", item.toString());
                }
            }
        }
    }
}
