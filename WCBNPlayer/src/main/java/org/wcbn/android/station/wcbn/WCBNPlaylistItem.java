package org.wcbn.android.station.wcbn;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.wcbn.android.R;

import java.util.List;

public class WCBNPlaylistItem implements Parcelable {

    private final String mTime, mArtist, mTitle, mAlbum, mLabel;

    WCBNPlaylistItem(Element element) {
        List<Element> elements = element.select("td");
        for(Element e : elements) {
            if(e.hasAttr("rowspan")) {
                elements.remove(e);
            }
        }

        mTime = elements.get(0).data();
        mArtist = elements.get(1).data();
        mTitle = elements.get(2).data();
        mAlbum = elements.get(3).data();
        mLabel = elements.get(4).data();
    }

    WCBNPlaylistItem(Parcel in) {
        mTime = in.readString();
        mArtist = in.readString();
        mTitle = in.readString();
        mAlbum = in.readString();
        mLabel = in.readString();
    }

    public View getView(Context context) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_playlist, null);

        // TODO
        // ((TextView) view.findViewById(R.id.time_text)).setText(mTime);
        // ((TextView) view.findViewById(R.id.artist_text)).setText(mArtist);
        // ((TextView) view.findViewById(R.id.title_text)).setText(mTitle);
        // ((TextView) view.findViewById(R.id.album_text)).setText(mAlbum);

        return view;
    }

    public static final Parcelable.Creator<WCBNPlaylistItem> CREATOR
            = new Parcelable.Creator<WCBNPlaylistItem>() {
        public WCBNPlaylistItem createFromParcel(Parcel in) {
            return new WCBNPlaylistItem(in);
        }

        public WCBNPlaylistItem[] newArray(int size) {
            return new WCBNPlaylistItem[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTime);
        dest.writeString(mArtist);
        dest.writeString(mTitle);
        dest.writeString(mAlbum);
        dest.writeString(mLabel);
    }
}
