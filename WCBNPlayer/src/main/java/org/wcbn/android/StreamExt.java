package org.wcbn.android;

import net.moraleboost.streamscraper.Stream;

/**
 * Extended version of Stream that supports additional attributes, like DJ name.
 *
 * @see net.moraleboost.streamscraper.Stream
 * @author Michael Huang
 */
public class StreamExt extends Stream {
    private String dj;
    private String program;
    private String artist;

    @Override
    public String toString()
    {
        return ("StreamExt(title=" + getTitle() +
                ", desc=" + getDescription() +
                ", uri=" + getUri() +
                ", lc=" + getCurrentListenerCount() +
                ", mlc=" + getMaxListenerCount() +
                ", plc=" + getPeakListenerCount() +
                ", br=" + getBitRate() +
                ", song=" + getCurrentSong() +
                ", mime=" + getContentType() +
                ", genre=" + getGenre() +
                ", artist=" + getArtist() +
                ", dj=" + getDj() +
                ", program=" +getProgram() +")");
    }

    @Override
    public void clear()
    {
        super.clear();
        dj = null;
        program = null;
    }

    @Override
    public void merge(Stream another) {
        super.merge(another);

        if(another instanceof StreamExt) {
            if(dj == null)
                dj = ((StreamExt) another).getDj();
            if(program == null)
                dj = ((StreamExt) another).getDj();
            if(artist == null)
                artist = ((StreamExt) another).getDj();
        }
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDj() {
        return dj;
    }

    public void setDj(String dj) {
        this.dj = dj;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

}
