package org.wcbn.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        Preference preference = findPreference("quality");

        assert preference != null;
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Integer i = Integer.parseInt((String) newValue);

                preference.setSummary(getResources()
                        .getStringArray(R.array.quality_desc)[i]);
                preference.setTitle(getResources()
                        .getStringArray(R.array.quality_pref)[i]);

                return true;
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Integer i = Integer.parseInt(prefs.getString("quality", "1"));
        preference.setSummary(getResources()
                .getStringArray(R.array.quality_desc)[i]);
        preference.setTitle(getResources()
                .getStringArray(R.array.quality_pref)[i]);
    }
}

