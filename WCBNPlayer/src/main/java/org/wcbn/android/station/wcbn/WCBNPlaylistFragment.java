package org.wcbn.android.station.wcbn;

import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import net.moraleboost.streamscraper.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.wcbn.android.station.Station;
import org.wcbn.android.UiFragment;

import java.io.IOException;

public class WCBNPlaylistFragment extends Fragment implements UiFragment {

    public static final String TAG = "WCBNPlaylistFragment";
    public static final String PLAYLIST_URI
            = "http://wcbn.org/ryan-playlist/searchplaylist.php?howmany=3&unit=hour";

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

                Elements elements = result
                        .select("tr.odd, tr.even");

            }
            else {
                // Loading failed
            }
        }
    }
}
