package org.wcbn.android.station;

import android.content.Context;
import android.util.Log;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.R;
import org.wcbn.android.Station;
import org.wcbn.android.StreamExt;
import org.wcbn.android.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WCBNStation implements Station {

    public static final int WEBSITE = R.string.wcbn_website;
    public static final int NUMBER = R.string.wcbn_number;

    static Pattern mSongPattern = Pattern.compile("\"(.*?)\"");
    static Pattern mArtistPattern = Pattern.compile(" by (.*?) on ");
    static Pattern mProgramPattern = Pattern.compile(" on (.*?) with ");

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
    public StreamExt fixMetadata(Stream stream) {
        StreamExt ext = new StreamExt();
        String currentSong = stream.getCurrentSong();
        String program, artist, song, dj = null;

        // currentSong is in the format: "song" by artist on program with dj
        // TODO: make the parser better at handling edge cases ie. no song, no artist etc.
        // "Droppin' Puns on Cspan!" by Vermin Supreme on Freeform with Greg H.
        // on Cspan!" by V...
        if(currentSong != null) {
            Log.d("WCBN", "Metadata string "+currentSong);

            Matcher songMatcher = mSongPattern.matcher(currentSong);
            songMatcher.find();
            song = songMatcher.group(1);

            Matcher artistMatcher = mArtistPattern.matcher(currentSong);
            int artistEnd;
            if(artistMatcher.find()) {
                artist = artistMatcher.group(1);
                artistEnd = artistMatcher.end();
            }

            else {
                artist = "";
                artistEnd = songMatcher.end();
            }

            // currentSong always contains a song, artist, and program, but sometimes not DJ
            if(currentSong.substring(artistEnd).contains(" with ")) {
                Matcher programMatcher = mProgramPattern
                        .matcher(currentSong);
                programMatcher.find(artistEnd - 4);
                program = programMatcher.group(1);

                dj = currentSong.substring(programMatcher.end());
            }
            else {
                program = currentSong.substring(artistEnd);
            }

            ext.setProgram(program);
            ext.setCurrentSong(song);
            ext.setDj(dj);
            ext.setArtist(artist);
        }

        ext.merge(stream);
        return ext;
    }
}
