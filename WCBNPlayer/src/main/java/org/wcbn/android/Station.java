package org.wcbn.android;

import net.moraleboost.streamscraper.Stream;

/**
 * Place all station-specific code in a Station.
 */
public interface Station {
    public int getWebsite();
    public StreamExt fixMetadata (Stream stream);
}
