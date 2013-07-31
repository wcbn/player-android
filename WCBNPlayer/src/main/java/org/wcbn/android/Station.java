package org.wcbn.android;

import net.moraleboost.streamscraper.Stream;

public interface Station {
    public int getWebsite();
    public StreamExt fixMetadata (Stream stream);
}
