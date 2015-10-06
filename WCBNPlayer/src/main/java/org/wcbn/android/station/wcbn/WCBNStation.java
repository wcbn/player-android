package org.wcbn.android.station.wcbn;

import android.content.Context;
import android.util.Log;

import net.moraleboost.streamscraper.Stream;

import org.jsoup.nodes.Element;
import org.wcbn.android.AlbumArtFragment;
import org.wcbn.android.R;
import org.wcbn.android.station.Station;
import org.wcbn.android.StreamExt;
import org.wcbn.android.UiFragment;
import org.wcbn.android.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WCBNStation implements Station {

    public static final String TAG = "WCBNStation";

    public static final int WEBSITE = R.string.wcbn_website;
    public static final int NUMBER = R.string.wcbn_number;
    public static final int TWITTER = R.string.wcbn_twitter_uri;
    public static final int FACEBOOK = R.string.wcbn_facebook_uri;
    public static final int TAB_NAMES = R.array.wcbn_tab_names;
    public static final String PLAYLIST_URI
            = "https://wcbn-readback.herokuapp.com/#now";

    static final List<Class<? extends UiFragment>> sFragments =
            new ArrayList<>();

    static {
        sFragments.add(AlbumArtFragment.class);
        sFragments.add(WCBNScheduleFragment.class);
        sFragments.add(WCBNPlaylistFragment.class);
        // ...
    }

    @Override
    public int getWebsite() {
        return WEBSITE;
    }

    @Override
    public int getNumber() {
        return NUMBER;
    }

    @Override
    public int getTwitter() { return TWITTER; }

    @Override
    public int getFacebook() { return FACEBOOK; }

    @Override
    public String getSongName(StreamExt stream, Context context) {
        return Utils.capitalizeTitle(stream.getCurrentSong());
    }

    @Override
    public String getArtistName(StreamExt stream, Context context) {
        return Utils.capitalizeTitle(stream.getArtist());
    }

    @Override
    public String getDescription(StreamExt stream, Context context) {
        StringBuilder subTextBuilder = new StringBuilder();
        subTextBuilder.append(context.getString(R.string.on))
                .append(" ")
                .append(stream.getProgram());

        if(stream.getDj() != null && stream.getDj().length() > 0) {
            subTextBuilder.append(" ")
                    .append(context.getString(R.string.with))
                    .append(" ")
                    .append(Utils.capitalizeTitle(stream.getDj()));
        }

        return subTextBuilder.toString();
    }

    @Override
    public StreamExt fixMetadata(List<Stream> streams) {
        Stream stream = streams.get(0);

        int currentListenerCount = 0;
        int peakListenerCount = 0;
        int maxListenerCount = 0;

        for(Stream s : streams) {
            currentListenerCount += s.getCurrentListenerCount();
            peakListenerCount += s.getPeakListenerCount();
            maxListenerCount += s.getMaxListenerCount();
        }

        StreamExt ext = new StreamExt();
        String program = "", artist = "", song = "", dj = "", album = "", recordLabel = "";

        try {
            Document doc = Jsoup.connect(PLAYLIST_URI)
                    .userAgent("Android")
                    .get();

            program = doc.select("h2").get(0).text();
            dj = doc.select("p.with").get(0).select("a").text();

            artist = doc.select("td.width3").get(0).text();
            song = doc.select("td.width4").get(0).text();
            album = doc.select("td.width3.italic").get(0).text();

        } catch (IOException e) {
            Log.d(TAG, "Error downloading playlist");
            e.printStackTrace();
        }

        ext.setProgram(program);
        ext.setCurrentSong(song);
        ext.setDj(dj);
        ext.setArtist(artist);
        ext.setAlbum(album);
        ext.setMaxListenerCount(maxListenerCount);
        ext.setCurrentListenerCount(currentListenerCount);
        ext.setPeakListenerCount(peakListenerCount);

        ext.merge(stream);
        return ext;
    }

    @Override
    public List<Class<? extends UiFragment>> getUiFragments() {
        return sFragments;
    }

    @Override
    public int getTabNames() {
        return TAB_NAMES;
    }
}
