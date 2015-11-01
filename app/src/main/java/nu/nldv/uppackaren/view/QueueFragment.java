package nu.nldv.uppackaren.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.UppackarenApplication;
import nu.nldv.uppackaren.event.StartFetchQueueEvent;
import nu.nldv.uppackaren.event.StartPollingForProgressEvent;
import nu.nldv.uppackaren.model.QueueItem;
import nu.nldv.uppackaren.util.QueueItemArrayAdapter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.inject.InjectView;

public class QueueFragment extends BaseFragment {

    public static final String TAG = QueueFragment.class.getSimpleName();

    @InjectView(R.id.queue_listview)
    ListView queueListView;

    private QueueItemArrayAdapter queueAdapter;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UppackarenApplication.getEventBus().register(this);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UppackarenApplication.getEventBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        queueAdapter = new QueueItemArrayAdapter(getActivity(), R.layout.queue_row_layout, new ArrayList<QueueItem>());
        queueAdapter.clear();
        queueListView.setAdapter(queueAdapter);
        startFetchingQueue(null);
    }

    @Subscribe
    public void startFetchingQueue(StartFetchQueueEvent event) {
        handler.removeCallbacks(fetchQueueRunnable);
        handler.postDelayed(fetchQueueRunnable, 100);
    }

    private Runnable fetchQueueRunnable = new Runnable() {
        @Override
        public void run() {
            getRestAPI().getQueue(new Callback<List<QueueItem>>() {
                @Override
                public void success(List<QueueItem> queueItems, Response response) {
                    queueAdapter.clear();
                    queueAdapter.addAll(queueItems);
                    queueAdapter.notifyDataSetChanged();
                    if (!queueItems.isEmpty()) {
                        handler.postDelayed(fetchQueueRunnable, 1000);
                        UppackarenApplication.getEventBus().post(new StartPollingForProgressEvent());
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    };
}
