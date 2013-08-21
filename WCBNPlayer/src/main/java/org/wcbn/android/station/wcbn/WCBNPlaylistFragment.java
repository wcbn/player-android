package org.wcbn.android.station.wcbn;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import net.moraleboost.streamscraper.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wcbn.android.R;
import org.wcbn.android.StreamService;
import org.wcbn.android.station.Station;
import org.wcbn.android.UiFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WCBNPlaylistFragment extends ListFragment implements UiFragment {

    private StreamService mService;
    private List<WCBNPlaylistItem> mItems;
    public static final String TAG = "WCBNPlaylistFragment";
    public static final String PLAYLIST_URI
            = "http://wcbn.org/ryan-playlist/searchplaylist.php?howmany=3&unit=hour";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_playlist, null);

        return view;
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

    @Override
    public void setService(Service service) {
        mService = (StreamService) service;
        if(mService.getPersistData().containsKey(TAG+".playlist_items")) {
            mItems = mService.getPersistData().getParcelableArrayList(TAG+".playlist_items");
        }
        else {
            new PlaylistUpdateTask().execute(PLAYLIST_URI);
        }
    }

    private class PlaylistAdapter extends ArrayAdapter<WCBNPlaylistItem> {

        private final List<WCBNPlaylistItem> mItems;
        private final Context mContext;

        public PlaylistAdapter(Context context, int resource, List<WCBNPlaylistItem> items) {
            super(context, resource, items);
            mItems = items;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mItems.get(position).getView(mContext);
        }
    }

    private class PlaylistUpdateTask extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... Uris) {
            Document doc;

            try {
                doc = Jsoup.connect(Uris[0])
                        .userAgent("Android")
                        .get();
            } catch(IOException e) {
                Log.d(TAG, "Error downloading playist");
                e.printStackTrace();
                return null;
            }
            return doc;
        }

        @Override
        public void onPostExecute(Document result) {
            if(result != null) {

                mItems = new ArrayList<WCBNPlaylistItem>();

                Elements elements = result
                        .select("tr.odd, tr.even");

                for(Element e : elements) {
                        WCBNPlaylistItem item = new WCBNPlaylistItem(e);
                        mItems.add(item);

                }

                setListAdapter(new PlaylistAdapter(mService, 0, mItems));
            }
            else {
                // Loading failed…
            }
        }
    }
}
