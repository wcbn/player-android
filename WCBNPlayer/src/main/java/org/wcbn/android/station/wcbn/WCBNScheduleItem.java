package org.wcbn.android.station.wcbn;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wcbn.android.R;

import java.util.regex.Matcher;

class WCBNScheduleItem implements Parcelable {
    private String mTime;
    private String mDj;
    private String mProgram;
    private String mUri;
    private ViewGroup mView;
    private LayoutInflater mInflater;
    private Typeface mTypeface;

    public WCBNScheduleItem(Context context) {
        initViews(context);
    }

    public void setLast(boolean last) {
        if(last) {
            mView.findViewById(R.id.line_1).setVisibility(View.INVISIBLE);
        }
        else {
            mView.findViewById(R.id.line_1).setVisibility(View.VISIBLE);
        }
    }

    public ViewGroup getView() {
        return mView;
    }

    @Override
    public String toString() {
        return "Program: "+mProgram+
                " DJ: "+mDj+
                " URI: "+mUri+
                " Time: "+mTime;
    }

    public void setLoading(boolean loading) {
        if(loading) {
            mView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
        }
        else {
            mView.findViewById(R.id.loading).setVisibility(View.GONE);
        }
    }

    public void setElement(Element element) {

        mProgram = null;
        mDj = null;
        mUri = null;
        mTime = null;

        Elements links = element.select("a[href]");
        if(!links.isEmpty()) {
            mUri = links.get(0).attr("href");
        }

        String text = element.text().trim();

        if(text != null) {
            Matcher djMatcher = WCBNScheduleFragment.sDjPattern.matcher(text);

            int timeStart = 0;

            if(djMatcher.find()) {
                mDj = djMatcher.group(1);
                Matcher programMatcher = WCBNScheduleFragment.sProgramPattern.matcher(text);
                if(programMatcher.find()) {
                    mProgram = programMatcher.group(1);
                }
                timeStart = djMatcher.end();
            }
            else {
                Matcher programMatcher = WCBNScheduleFragment.sProgramPatternNoDj.matcher(text);
                if(programMatcher.find()) {
                    mProgram = programMatcher.group(1);
                }
                timeStart = programMatcher.end();
            }

            if(timeStart != 0) {
                mTime = text.substring(timeStart - 5).trim();
                String[] times =  mTime.split("-");
                String startTime = times[0];
                String endTime = times[1];

                if(!(startTime.contains("am") || startTime.contains("pm"))) {
                    if(endTime.contains("am")) {
                        startTime = startTime + "am";
                    }
                    else if(endTime.contains("pm")) {
                        startTime = startTime + "pm";
                    }
                }

                mTime = startTime + " - " + endTime;
            }
        }

        updateViews();
    }

    public void initViews(Context context) {
        mTypeface = Typeface.createFromAsset(context.getAssets()
                , "Roboto-Light.ttf");
        mInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        mView = (ViewGroup) mInflater.inflate(R.layout.item_schedule, null);

        ((TextView) mView.findViewById(R.id.time_text)).setTypeface(mTypeface);
        ((TextView) mView.findViewById(R.id.program_text)).setTypeface(mTypeface);
        ((TextView) mView.findViewById(R.id.dj_text)).setTypeface(mTypeface);
    }

    public void updateViews() {
        // Update our views
        if(mDj != null) {
            mView.findViewById(R.id.dj_text).setVisibility(View.VISIBLE);
            ((TextView) mView.findViewById(R.id.dj_text)).setText(mDj);
        }
        else {
            mView.findViewById(R.id.dj_text).setVisibility(View.INVISIBLE);
        }
        ((TextView) mView.findViewById(R.id.program_text)).setText(mProgram);
        ((TextView) mView.findViewById(R.id.time_text)).setText(mTime);
        if(mUri != null) {
            ((ImageView) mView.findViewById(R.id.icon_link))
                    .setImageResource(R.drawable.ic_menu_globe);
            mView.findViewById(R.id.btn_link).setClickable(true);
            mView.findViewById(R.id.btn_link).setFocusable(true);
            mView.findViewById(R.id.btn_link).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInflater.getContext().startActivity(new Intent()
                            .setAction(Intent.ACTION_VIEW)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setData(Uri.parse(mUri)));
                }
            });
        }
        else {
            ((ImageView) mView.findViewById(R.id.icon_link))
                    .setImageResource(R.drawable.ic_menu_globe_disabled);
            mView.findViewById(R.id.btn_link).setClickable(false);
            mView.findViewById(R.id.btn_link).setFocusable(false);
        }
    }

    public String getTime() {
        return mTime;
    }


    public String getDj() {
        return mDj;
    }

    public String getProgram() {
        return mProgram;
    }

    public String getUri() {
        return mUri;
    }

    @Override
    public boolean equals(Object item) {
        if (item == null)
            return false;
        if (item == this)
            return true;
        if (!(item instanceof WCBNScheduleItem))
            return false;

        WCBNScheduleItem other = (WCBNScheduleItem) item;

        if(other.getTime().equals(mTime) &&
                other.getDj().equals(mDj) &&
                other.getProgram().equals(mProgram) &&
                other.getUri().equals(mUri)) {

            return true;
        }
        return false;
    }

    public WCBNScheduleItem(Parcel in) {
        mTime = in.readString();
        mDj = in.readString();
        mProgram = in.readString();
        mUri = in.readString();

    }

    public static final Parcelable.Creator<WCBNScheduleItem> CREATOR
            = new Parcelable.Creator<WCBNScheduleItem>() {
        public WCBNScheduleItem createFromParcel(Parcel in) {
            return new WCBNScheduleItem(in);
        }

        public WCBNScheduleItem[] newArray(int size) {
            return new WCBNScheduleItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString("mTime");
        dest.writeString("mDj");
        dest.writeString("mProgram");
        dest.writeString("mUri");
    }
}