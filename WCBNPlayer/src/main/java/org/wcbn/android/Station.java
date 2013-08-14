package org.wcbn.android;

import android.content.Context;

import net.moraleboost.streamscraper.Stream;

/**
 * Place all station-specific code in a Station.
 */
public interface Station {
    public int getWebsite();
    public int getNumber();
    public String getSongName(StreamExt stream, Context context);
    public String getArtistName(StreamExt stream, Context context);
    public String getDescription(StreamExt stream, Context context);
    public StreamExt fixMetadata (Stream stream);
}
