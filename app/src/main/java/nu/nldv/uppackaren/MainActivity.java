package nu.nldv.uppackaren;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nu.nldv.uppackaren.model.RarArchive;
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

    private List<RarArchive> list;
    private RarArchiveArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RarArchiveArrayAdapter(this, R.layout.row_layout, new ArrayList<RarArchive>());
        adapter.clear();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (serverUriIsSet()) loadData();
        else setServerUri();
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RarArchive rarArchive = list.get(position);
        getRestAPI().unRar(rarArchive.getId(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Toast.makeText(getApplicationContext(), "Successfully unrared " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Failed to unrar", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
