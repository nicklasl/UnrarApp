package nu.nldv.uppackaren;

import android.app.Application;

import com.squareup.otto.Bus;

import nu.nldv.uppackaren.util.RestAPI;
import retrofit.RestAdapter;

public class UppackarenApplication extends Application {

    public static final String UPPACKAREN = "uppackaren_shared_prefs";
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
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(getEndPoint())
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .build();

            restApiInstance = restAdapter.create(RestAPI.class);
        }
        return restApiInstance;
    }

    private static String getEndPoint() {
        return instance.getSharedPreferences(UPPACKAREN, MODE_PRIVATE).getString(UPPACKAREN_SERVER_URI, "http://localhost:8080");
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
