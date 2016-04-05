package nu.nldv.uppackaren;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import nu.nldv.uppackaren.event.ReloadArchivesEvent;
import nu.nldv.uppackaren.model.RarArchive;
import nu.nldv.uppackaren.view.ArchivesFragment;
import nu.nldv.uppackaren.view.ProgressFragment;
import nu.nldv.uppackaren.view.QueueFragment;
import roboguice.inject.ContentView;


@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {


    private static final String ROOT = "root";
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
            fragmentTransaction.addToBackStack(ROOT);
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
            UppackarenApplication.getEventBus().post(new ReloadArchivesEvent());
            return true;
        } else if(id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void setServerUri() {
        Intent startSearchForServerActivity = new Intent(this, SearchForServerActivity.class);
        startActivity(startSearchForServerActivity);
    }

    @Override
    public void onBackPressed() {
        final int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
        if(backStackEntryCount <= 1) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }

    }

    public void pushFragment(RarArchive rarArchive) {
        final ArchivesFragment fragment = ArchivesFragment.newInstance(rarArchive);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.archive_list_container, fragment)
                .addToBackStack(rarArchive.getId())
                .commitAllowingStateLoss();
    }
}
