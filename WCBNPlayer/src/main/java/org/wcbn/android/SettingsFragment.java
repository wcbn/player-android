package org.wcbn.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.wcbn.android.station.Station;

public class SettingsFragment extends PreferenceFragment {

    private Station mStation = Utils.getStation();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        Preference qualityPreference = findPreference("quality");

        assert qualityPreference != null;
        qualityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Integer i = Integer.parseInt((String) newValue);

                preference.setSummary(getResources()
                        .getStringArray(R.array.quality_desc)[i]);
                preference.setTitle(getResources()
                        .getStringArray(R.array.quality_pref)[i]);

                resetService();

                return true;
            }
        });

        Preference albumArtPreference = findPreference("grab_album_art");
        assert albumArtPreference != null;
        albumArtPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                resetService();
                return true;
            }
        });

        Preference websitePreference = findPreference("website");
        assert websitePreference != null;
        websitePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                startActivity(new Intent()
                        .setAction(Intent.ACTION_VIEW)
                        .setData(Uri.parse(getString(mStation.getWebsite()))));

                return false;
            }
        });

        Preference numberPreference = findPreference("request_number");
        assert numberPreference != null;
        numberPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                startActivity(new Intent()
                        .setAction(Intent.ACTION_DIAL)
                        .setData(Uri.parse("tel:"+getString(mStation.getNumber()))));

                return false;
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Integer i = Integer.parseInt(prefs.getString("quality", "1"));
        qualityPreference.setSummary(getResources()
                .getStringArray(R.array.quality_desc)[i]);
        qualityPreference.setTitle(getResources()
                .getStringArray(R.array.quality_pref)[i]);
    }

    public void resetService() {
        if(getActivity() != null) {
            getActivity().stopService(new Intent(getActivity(), StreamService.class));
            getActivity().startService(new Intent(getActivity(), StreamService.class));
        }
    }
}

