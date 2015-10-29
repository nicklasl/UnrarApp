package nu.nldv.uppackaren.view;

import nu.nldv.uppackaren.UppackarenApplication;
import nu.nldv.uppackaren.util.RestAPI;
import roboguice.fragment.provided.RoboFragment;

public abstract class BaseFragment extends RoboFragment {
    protected RestAPI getRestAPI() {
        return UppackarenApplication.restApi();
    }

}
