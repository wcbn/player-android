package org.wcbn.android.station;

import android.content.Context;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.StreamExt;
import org.wcbn.android.UiFragment;

import java.util.List;

/**
 * Represents a Station.
 *
 * Place all station-specific code in a Station and associated classes.
 */
public interface Station {
    int getWebsite();
    int getNumber();
    int getTabNames();
    String getSongName(StreamExt stream, Context context);
    String getArtistName(StreamExt stream, Context context);
    String getDescription(StreamExt stream, Context context);
    StreamExt fixMetadata (List<Stream> streams);
    List<Class<? extends UiFragment>> getUiFragments();
}
