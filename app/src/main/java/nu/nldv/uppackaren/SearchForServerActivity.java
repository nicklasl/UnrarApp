package nu.nldv.uppackaren;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.List;

import nu.nldv.uppackaren.model.Server;
import nu.nldv.uppackaren.util.ServerScanner;
import nu.nldv.uppackaren.util.ServerScannerCallback;
import nu.nldv.uppackaren.view.AddManuallyDialogFragment;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_search_for_server)
public class SearchForServerActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final Integer PORT = 8080;
    private static final String SUBNET = "192.168.1"; //TODO figure this out on its own!

    @InjectView(R.id.search_for_server_scan_result_listview)
    ListView listView;
    @InjectView(R.id.search_for_server_start_scan_button)
    Button startScanButton;
    @InjectView(R.id.search_for_server_progressbar)
    ProgressBar progressBar;

    private ArrayAdapter<Server> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<Server>(this, R.layout.row_layout, R.id.title_textview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void startScan(View view) {
        progressBar.setVisibility(View.VISIBLE);
        startScanButton.setVisibility(View.GONE);
        String subnet = SUBNET;
        ServerScanner scanner = new ServerScanner(subnet, new ServerScannerCallback() {
            @Override
            public void callback(List<Server> servers) {
                Log.d("Uppackaren", "done. result size=" + servers.size());
                progressBar.setVisibility(View.GONE);
                adapter.addAll(servers);
                adapter.notifyDataSetChanged();
                listView.setVisibility(View.VISIBLE);
            }
        });
        scanner.execute(PORT);
    }

    private void storeServerUri(String serverUri) {
        sharedPrefs().edit().putString(UppackarenApplication.UPPACKAREN_SERVER_URI, serverUri).commit();
        ((UppackarenApplication) getApplication()).resetRestApi();
        finish();
    }

    public void showManualContainer(View view) {
        final AddManuallyDialogFragment fragment = AddManuallyDialogFragment.newInstance(new AddManuallyDialogFragment.Handler() {
            @Override
            public void handleOk(String result) {

                storeServerUri(result);
            }
        });

        fragment.show(getFragmentManager(), AddManuallyDialogFragment.TAG);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Server item = adapter.getItem(position);
        storeServerUri("http://" + item.getIp() + ":" + item.getPort());
    }
}
