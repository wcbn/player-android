package org.wcbn.android;

import android.content.Context;

import net.moraleboost.streamscraper.Stream;

import java.util.List;

/**
 * Represents a Station.
 *
 * Place all station-specific code in a Station and associated classes.
 */
public interface Station {
    public int getWebsite();
    public int getNumber();
    public int getTabNames();
    public String getSongName(StreamExt stream, Context context);
    public String getArtistName(StreamExt stream, Context context);
    public String getDescription(StreamExt stream, Context context);
    public StreamExt fixMetadata (Stream stream);
    public List<Class<? extends UiFragment>> getUiFragments();
}
