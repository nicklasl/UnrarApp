package nu.nldv.uppackaren;

import android.app.Application;

import nu.nldv.uppackaren.util.RestAPI;
import retrofit.RestAdapter;

public class UppackarenApplication extends Application {

    public static final String UPPACKAREN = "uppackaren_shared_prefs";
    public static final String UPPACKAREN_SERVER_URI = "uppackaren_server_uri";
    private RestAPI restApiInstance;


    public RestAPI restApi() {
        if (restApiInstance == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(getSharedPreferences(UPPACKAREN, MODE_PRIVATE).getString(UPPACKAREN_SERVER_URI, "http://localhost:8080"))
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();

            restApiInstance = restAdapter.create(RestAPI.class);
        }
        return restApiInstance;
    }

    public void resetRestApi() {
        restApiInstance = null;
    }
}
