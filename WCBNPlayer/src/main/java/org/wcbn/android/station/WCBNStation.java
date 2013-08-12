package org.wcbn.android.station;

import android.util.Log;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.R;
import org.wcbn.android.Station;
import org.wcbn.android.StreamExt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WCBNStation implements Station {

    public static final int WEBSITE = R.string.wcbn_website;

    static Pattern mSongPattern = Pattern.compile("\"(.*?)\"");
    static Pattern mArtistPattern = Pattern.compile(" by (.*?) on ");
    static Pattern mProgramPattern = Pattern.compile(" on (.*?) with ");

    @Override
    public int getWebsite() {
        return WEBSITE;
    }

    @Override
    public StreamExt fixMetadata(Stream stream) {
        StreamExt ext = new StreamExt();
        String currentSong = stream.getCurrentSong();
        String program, artist, song, dj = null;

        // currentSong is in the format: "song" by artist on program with dj
        // TODO: make the parser better at handling edge cases ie. no song, no artist etc.
        if(currentSong != null) {
            Log.d("WCBN", "Metadata string "+currentSong);

            Matcher songMatcher = mSongPattern.matcher(currentSong);
            songMatcher.find();
            song = songMatcher.group(1);

            Matcher artistMatcher = mArtistPattern.matcher(currentSong);
            artistMatcher.find();
            artist = artistMatcher.group(1);

            // currentSong always contains a song, artist, and program, but sometimes not DJ
            if(currentSong.substring(artistMatcher.end()).contains("with")) {
                Matcher programMatcher = mProgramPattern.matcher(currentSong);
                programMatcher.find();
                program = programMatcher.group(1);

                dj = currentSong.substring(programMatcher.end());
            }
            else {
                program = currentSong.substring(artistMatcher.end());
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
