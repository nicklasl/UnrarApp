package nu.nldv.uppackaren.view;

import android.os.Bundle;

import nu.nldv.uppackaren.UppackarenApplication;
import nu.nldv.uppackaren.util.RestAPI;
import roboguice.fragment.provided.RoboFragment;

public abstract class BaseFragment extends RoboFragment {
    protected RestAPI getRestAPI() {
        return UppackarenApplication.restApi();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UppackarenApplication.getEventBus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UppackarenApplication.getEventBus().unregister(this);
    }
}
