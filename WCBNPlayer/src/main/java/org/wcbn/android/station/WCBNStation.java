package org.wcbn.android.station;

import net.moraleboost.streamscraper.Stream;

import org.wcbn.android.R;
import org.wcbn.android.Station;
import org.wcbn.android.StreamExt;

public class WCBNStation implements Station {

    public static final int WEBSITE = R.string.wcbn_website;

    @Override
    public int getWebsite() {
        return WEBSITE;
    }

    @Override
    public StreamExt fixMetadata(Stream stream) {
        StreamExt ext = new StreamExt();
        ext.setDj(null);
        ext.merge(stream);
        return ext;
    }
}
