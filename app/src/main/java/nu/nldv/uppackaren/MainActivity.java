package nu.nldv.uppackaren;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nu.nldv.uppackaren.model.RarArchive;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.activity.RoboListActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboListActivity {

    @InjectView(R.id.loader)
    ProgressBar loader;

    private List<RarArchive> list;
    private RarArchiveArrayAdapter adapter;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        RarArchive rarArchive = list.get(position);
        ((UppackarenApplication) getApplication()).restApi().unRar(rarArchive.getId(), new Callback<String>() {
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
        View alertDialogLayout = getLayoutInflater().inflate(R.layout.set_server_uri_layout, null);
        final EditText editText = (EditText) alertDialogLayout.findViewById(R.id.server_uri_edittext);
        if (sharedPrefs().contains(UppackarenApplication.UPPACKAREN_SERVER_URI)) {
            editText.setText(sharedPrefs().getString(UppackarenApplication.UPPACKAREN_SERVER_URI, "not set"));
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_server_uri)
                .setView(alertDialogLayout)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String serverUri = editText.getText().toString();
                        sharedPrefs().edit().putString(UppackarenApplication.UPPACKAREN_SERVER_URI, serverUri).commit();
                        ((UppackarenApplication) getApplication()).resetRestApi();
                        dialog.dismiss();
                        loadData();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private SharedPreferences sharedPrefs() {
        return getSharedPreferences(UppackarenApplication.UPPACKAREN, MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RarArchiveArrayAdapter(this, R.layout.row_layout, new ArrayList<RarArchive>());
        setListAdapter(adapter);
        if (serverUriIsSet()) {
            loadData();
        } else {
            setServerUri();
        }
    }

    private boolean serverUriIsSet() {
        SharedPreferences sharedPreferences = sharedPrefs();
        return sharedPreferences.contains(UppackarenApplication.UPPACKAREN_SERVER_URI);
    }

    private void loadData() {
        loader.setVisibility(View.VISIBLE);
        ((UppackarenApplication) getApplication()).restApi().getRarArchives(new Callback<List<RarArchive>>() {
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

}
