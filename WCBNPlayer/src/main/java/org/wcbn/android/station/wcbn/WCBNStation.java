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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WCBNStation implements Station {

    public static final String TAG = "WCBNStation";

    public static final int WEBSITE = R.string.wcbn_website;
    public static final int NUMBER = R.string.wcbn_number;
    public static final int TAB_NAMES = R.array.wcbn_tab_names;
    public static final String PLAYLIST_URI
            = "http://wcbn.org/ryan-playlist/searchplaylist.php?howmany=1&unit=hour";

    static final List<Class<? extends UiFragment>> sFragments =
            new ArrayList<Class<? extends UiFragment>>();

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

        if(stream.getDj() != null) {
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
        String program = "", artist = "", song = "", dj = "";

        try {
            Document doc = Jsoup.connect(PLAYLIST_URI)
                    .userAgent("Android")
                    .get();

            String[] djProgram = doc
                    .select("td.show")
                    .get(0)
                    .text()
                    .split("\n")[0]
                    .split(", with ");
            program = djProgram[0];
            dj = djProgram[1].substring(0, djProgram[1].length()-1).trim();

            List<Element> elements = doc.select("tr.odd, tr.even").get(0).select("td");
            artist = elements.get(4).text().trim();
            song = elements.get(5).text().trim();

        } catch (IOException e) {
            Log.d(TAG, "Error downloading playlist");
            e.printStackTrace();
        }

        ext.setProgram(program);
        ext.setCurrentSong(song);
        ext.setDj(dj);
        ext.setArtist(artist);
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
