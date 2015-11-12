package nu.nldv.uppackaren;

import android.app.FragmentTransaction;
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
import nu.nldv.uppackaren.view.ArchivesFragment;
import nu.nldv.uppackaren.view.ProgressFragment;
import nu.nldv.uppackaren.view.QueueFragment;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {


    private Handler handler;
    private ArchivesFragment archivesFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());

        if (!serverUriIsSet()) {
            setServerUri();
        }

        archivesFragment = new ArchivesFragment();
        QueueFragment queueFragment = new QueueFragment();
        ProgressFragment progressFragment = new ProgressFragment();
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(getFragmentManager().findFragmentByTag(ArchivesFragment.TAG) == null) {
            fragmentTransaction.add(R.id.archive_list_container, archivesFragment, ArchivesFragment.TAG);
        }
        if(getFragmentManager().findFragmentByTag(QueueFragment.TAG) == null){
            fragmentTransaction.add(R.id.queue_container, queueFragment, QueueFragment.TAG);
        }
        if(getFragmentManager().findFragmentByTag(ProgressFragment.TAG) == null){
            fragmentTransaction.add(R.id.current_work_container, progressFragment, ProgressFragment.TAG);
        }
        fragmentTransaction.commit();
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
            archivesFragment.loadData();
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

}
