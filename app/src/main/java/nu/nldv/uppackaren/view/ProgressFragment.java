package nu.nldv.uppackaren.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.UppackarenApplication;
import nu.nldv.uppackaren.event.ReloadArchivesEvent;
import nu.nldv.uppackaren.event.StartPollingForProgressEvent;
import nu.nldv.uppackaren.model.StatusResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.inject.InjectView;

public class ProgressFragment extends BaseFragment {

    public static final String TAG = ProgressFragment.class.getSimpleName();

    @InjectView(R.id.current_work_progressbar)
    ProgressBar currentWorkProgress;
    @InjectView(R.id.current_work_textview)
    TextView currentWorkTextView;

    private Handler handler;
    private int retries = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_work, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resetCurrentWorkView();
    }

    private void resetCurrentWorkView() {
        if(isAdded()) {
            currentWorkTextView.setText(getString(R.string.current_work, getString(R.string.idle)));
            currentWorkProgress.setProgress(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startPollingForProgress(null);
    }

    @Subscribe
    public void startPollingForProgress(StartPollingForProgressEvent event) {
        retries = 0;
        handler.removeCallbacks(fetchStatusAndUpdateProgress);
        handler.postDelayed(fetchStatusAndUpdateProgress, 500);
    }


    private Runnable fetchStatusAndUpdateProgress = new Runnable() {
        @Override
        public void run() {
            getRestAPI().getStatus(new Callback<StatusResponse>() {

                @Override
                public void success(StatusResponse statusResponse, Response response) {
                    if(isAdded()) {
                        currentWorkTextView.setText(getString(R.string.current_work, statusResponse.getFileName()));
                        currentWorkProgress.setProgress(statusResponse.getPercentDone());
                    }
                    handler.postDelayed(fetchStatusAndUpdateProgress, 5000);
                }

                @Override
                public void failure(RetrofitError error) {
                    if(retries < 3) {
                        handler.postDelayed(fetchStatusAndUpdateProgress, 5000);
                    } else {
                        UppackarenApplication.getEventBus().post(new ReloadArchivesEvent());
                        resetCurrentWorkView();
                    }
                    retries ++;
                }
            });
        }
    };
}