package org.wcbn.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {

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
                        .setData(Uri.parse(getString(R.string.wcbn_website))));

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
                        .setData(Uri.parse("tel:"+getString(R.string.wcbn_number))));

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
}

