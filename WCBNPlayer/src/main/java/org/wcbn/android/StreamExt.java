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
                ", dj=" + getDj() + ")");
    }

    @Override
    public void clear()
    {
        super.clear();
        dj = null;
    }

    @Override
    public void merge(Stream another) {
        super.merge(another);

        if(dj == null && another instanceof StreamExt)
            dj = ((StreamExt) another).getDj();
    }

    public String getDj() {
        return dj;
    }

    public void setDj(String dj) {
        this.dj = dj;
    }

}
