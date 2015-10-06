package nu.nldv.uppackaren;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nu.nldv.uppackaren.model.QueueItem;
import nu.nldv.uppackaren.model.RarArchive;
import nu.nldv.uppackaren.model.StatusResponse;
import nu.nldv.uppackaren.model.UnrarResponse;
import nu.nldv.uppackaren.util.QueueItemArrayAdapter;
import nu.nldv.uppackaren.util.RarArchiveArrayAdapter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @InjectView(R.id.loader)
    ProgressBar loader;
    @InjectView(R.id.listview)
    ListView listView;
    @InjectView(R.id.queue_listview)
    ListView queueListView;
    @InjectView(R.id.current_work_container)
    ViewGroup currentWorkContainer;
    @InjectView(R.id.current_work_progressbar)
    ProgressBar currentWorkProgress;
    @InjectView(R.id.queue_container)
    ViewGroup queueContainer;

    private List<RarArchive> list;
    private RarArchiveArrayAdapter adapter;
    private Handler handler;
    private QueueItemArrayAdapter queueAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RarArchiveArrayAdapter(this, R.layout.row_layout, new ArrayList<RarArchive>());
        adapter.clear();
        queueAdapter = new QueueItemArrayAdapter(this, R.layout.queue_row_layout, new ArrayList<QueueItem>());
        queueAdapter.clear();
        listView.setAdapter(adapter);
        queueListView.setAdapter(queueAdapter);
        listView.setOnItemClickListener(this);
        handler = new Handler(Looper.getMainLooper());

        if (serverUriIsSet()) {
            loadData();
        } else {
            setServerUri();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            loadData();
            return true;
        } else if (id == R.id.action_change_server_uri) {
            setServerUri();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setServerUri() {
        Intent startSearchForServerActivity = new Intent(this, SearchForServerActivity.class);
        startActivity(startSearchForServerActivity);
    }


    private void loadData() {
        loader.setVisibility(View.VISIBLE);
        getRestAPI().getRarArchives(new Callback<List<RarArchive>>() {
            @Override
            public void success(List<RarArchive> rarArchives, Response response) {
                list = rarArchives;
                adapter.clear();
                adapter.addAll(rarArchives);
                adapter.notifyDataSetChanged();
                loader.setVisibility(View.GONE);
            }

            @Override
            public void failure(RetrofitError error) {
                loader.setVisibility(View.GONE);
            }
        });
        startFetchingQueue();
    }

    private void startFetchingQueue() {
        handler.removeCallbacks(fetchQueueRunnable);
        handler.postDelayed(fetchQueueRunnable, 1000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        final RarArchive rarArchive = list.get(position);
        getRestAPI().unRar(rarArchive.getId(), new Callback<UnrarResponse>() {
            @Override
            public void success(UnrarResponse unrarResponse, Response response) {
                Toast.makeText(getApplicationContext(), "Added " + unrarResponse.getQueueId() + " to queue.", Toast.LENGTH_SHORT).show();
                list.remove(position);
                adapter.remove(rarArchive);
                adapter.notifyDataSetChanged();
                startPollingForProgress();
                startFetchingQueue();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Failed to unrar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPollingForProgress() {
        currentWorkContainer.setVisibility(View.VISIBLE);
        handler.removeCallbacks(fetchStatusAndUpdateProgress);
        handler.postDelayed(fetchStatusAndUpdateProgress, 1000);
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
                    if (queueItems.isEmpty()) {
                        queueContainer.setVisibility(View.GONE);
                    } else {
                        queueContainer.setVisibility(View.VISIBLE);
                        handler.postDelayed(fetchQueueRunnable, 1000);
                        startPollingForProgress();
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    };

    private Runnable fetchStatusAndUpdateProgress = new Runnable() {
        @Override
        public void run() {
            getRestAPI().getStatus(new Callback<StatusResponse>() {

                @Override
                public void success(StatusResponse statusResponse, Response response) {
                    currentWorkProgress.setProgress(statusResponse.getPercentDone());
                    handler.postDelayed(fetchStatusAndUpdateProgress, 1000);
                }

                @Override
                public void failure(RetrofitError error) {
                    currentWorkContainer.setVisibility(View.GONE);
                    loadData();
                }
            });
        }
    };
}
