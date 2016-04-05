package nu.nldv.uppackaren.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.SearchForServerActivity;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final Preference preference = findPreference("change_server_uri_key");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setServerUri();
                return true;
            }
        });
    }

    private void setServerUri() {
        Intent startSearchForServerActivity = new Intent(getActivity(), SearchForServerActivity.class);
        startActivity(startSearchForServerActivity);
    }

}
