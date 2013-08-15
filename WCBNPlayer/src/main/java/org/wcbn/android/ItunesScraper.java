package org.wcbn.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Scrapes album art and metadata, specifically from iTunes.
 */
public class ItunesScraper {
    public static final Uri URI_BASE =
            Uri.parse("https://itunes.apple.com/search?limit=1&version=2");
    private final Uri mUri;
    private final String mSearchTerm;
    private final JSONObject mObj;

    ItunesScraper(String searchTerm, String entity) {
        mSearchTerm = searchTerm;

        mUri = URI_BASE.buildUpon()
            .appendQueryParameter("term", searchTerm)
            .appendQueryParameter("entity", entity)
            .build();

        mObj = query();
    }

    public String getSearchTerm() {
        return mSearchTerm;
    }

    public String getArtist() {
        if(mObj == null) {
            return null;
        }
        try {
            return mObj.getJSONArray("results")
                    .getJSONObject(0)
                    .getString("artistName");
        } catch(JSONException e) {
            return null;
        }
    }

    public String getTrack() {
        if(mObj == null) {
            return null;
        }
        try {
            return mObj.getJSONArray("results")
                    .getJSONObject(0)
                    .getString("trackName");
        } catch(JSONException e) {
            return null;
        }
    }

    public Bitmap getLargeAlbumArt() {
        if(mObj == null) {
            return null;
        }

        String artUri;
        try {
            artUri = mObj.getJSONArray("results")
                    .getJSONObject(0) // First result
                    .getString("artworkUrl100") // 100x100 album art
                    .replaceAll("100x100-75.jpg", "600x600-75.jpg"); //Get 600x600 URI
        }
        catch(JSONException e) {
            return null;
        }

        try {
            URL url = new URL(artUri);
            InputStream in = url.openConnection().getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in,1024*8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while((len = bis.read(buffer)) != -1){
                out.write(buffer, 0, len);
            }
            out.close();
            bis.close();

            byte[] data = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private JSONObject query() {
        try {
            InputStream is = new URL(mUri.toString()).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    Charset.forName("UTF-8")));

            StringBuilder total = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
            JSONObject json = new JSONObject(total.toString());
            is.close();
            return json;
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        catch(JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
