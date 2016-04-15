package nu.nldv.uppackaren;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;

import nu.nldv.uppackaren.util.RestAPI;
import retrofit.RestAdapter;

public class UppackarenApplication extends Application {

    public static final String UPPACKAREN_SERVER_URI = "uppackaren_server_uri";
    private static RestAPI restApiInstance;
    private static UppackarenApplication instance;
    private static Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static RestAPI restApi() {
        if (restApiInstance == null) {
            restApiInstance = restAPIForEndPoint(getEndPoint());
        }
        return restApiInstance;
    }

    public static RestAPI restAPIForEndPoint(String endpoint) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint.concat("/api"))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();
        return restAdapter.create(RestAPI.class);
    }

    private static String getEndPoint() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(instance);
        return preferences.getString(UPPACKAREN_SERVER_URI, "http://localhost:8080");
    }

    public static Bus getEventBus() {
        if(bus == null) {
          bus = new Bus();
        }
        return bus;
    }

    public void resetRestApi() {
        restApiInstance = null;
    }
}
