package nu.nldv.uppackaren;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import nu.nldv.uppackaren.util.RestAPI;
import roboguice.activity.RoboActivity;

public class BaseActivity extends RoboActivity {
    protected RestAPI getRestAPI() {
        return ((UppackarenApplication) getApplication()).restApi();
    }

    public SharedPreferences sharedPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }

    protected boolean serverUriIsSet() {
        SharedPreferences sharedPreferences = sharedPrefs();
        return sharedPreferences.contains(UppackarenApplication.UPPACKAREN_SERVER_URI);
    }

}
