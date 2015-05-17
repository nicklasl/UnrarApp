package nu.nldv.uppackaren;

import android.content.SharedPreferences;

import nu.nldv.uppackaren.util.RestAPI;
import roboguice.activity.RoboActivity;

public class BaseActivity extends RoboActivity {
    protected RestAPI getRestAPI() {
        return ((UppackarenApplication) getApplication()).restApi();
    }

    protected SharedPreferences sharedPrefs() {
        return getSharedPreferences(UppackarenApplication.UPPACKAREN, MODE_PRIVATE);
    }

    protected boolean serverUriIsSet() {
        SharedPreferences sharedPreferences = sharedPrefs();
        return sharedPreferences.contains(UppackarenApplication.UPPACKAREN_SERVER_URI);
    }

}
