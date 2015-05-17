package nu.nldv.uppackaren;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_search_for_server)
public class SearchForServerActivity extends BaseActivity {

    @InjectView(R.id.search_for_server_scan_result_listview)
    ListView listView;
    @InjectView(R.id.search_for_server_start_scan_button)
    Button startScanButton;
    @InjectView(R.id.search_for_server_progressbar)
    ProgressBar progressBar;
    @InjectView(R.id.search_for_server_add_manually_edittext)
    EditText manualEditText;
    @InjectView(R.id.search_for_server_add_manually_button)
    Button addManuallyButton;
    @InjectView(R.id.search_for_server_add_manually_container)
    RelativeLayout addManuallyContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sharedPrefs().contains(UppackarenApplication.UPPACKAREN_SERVER_URI)) {
            manualEditText.setText(sharedPrefs().getString(UppackarenApplication.UPPACKAREN_SERVER_URI, "not set"));
        }
    }

    public void startScan(View view) {
        progressBar.setVisibility(View.VISIBLE);
        startScanButton.setVisibility(View.GONE);
    }

    public void okManual(View view) {
        String serverUri = manualEditText.getText().toString();
        sharedPrefs().edit().putString(UppackarenApplication.UPPACKAREN_SERVER_URI, serverUri).commit();
        ((UppackarenApplication) getApplication()).resetRestApi();
        finish();
    }

    public void showManualContainer(View view) {
        addManuallyButton.setVisibility(View.GONE);
        addManuallyContainer.setVisibility(View.VISIBLE);
    }
}
