package nu.nldv.uppackaren.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.UppackarenApplication;
import nu.nldv.uppackaren.event.StartFetchQueueEvent;
import nu.nldv.uppackaren.event.StartPollingForProgressEvent;
import nu.nldv.uppackaren.model.RarArchive;
import nu.nldv.uppackaren.model.UnrarResponse;
import nu.nldv.uppackaren.util.RarArchiveArrayAdapter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.inject.InjectView;

public class ArchivesFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    public static final String TAG = ArchivesFragment.class.getSimpleName();

    @InjectView(R.id.listview)
    ListView listView;

    private List<RarArchive> list;
    private RarArchiveArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RarArchiveArrayAdapter(getActivity(), R.layout.row_layout, new ArrayList<RarArchive>());
        adapter.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_archives, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    public void loadData() {
        getRestAPI().getRarArchives(new Callback<List<RarArchive>>() {
            @Override
            public void success(List<RarArchive> rarArchives, Response response) {
                list = rarArchives;
                adapter.clear();
                adapter.addAll(rarArchives);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        final RarArchive rarArchive = list.get(position);
        getRestAPI().unRar(rarArchive.getId(), new Callback<UnrarResponse>() {
            @Override
            public void success(UnrarResponse unrarResponse, Response response) {
                Toast.makeText(getActivity(), "Added " + unrarResponse.getQueueId() + " to queue.", Toast.LENGTH_SHORT).show();
                list.remove(position);
                adapter.remove(rarArchive);
                adapter.notifyDataSetChanged();
                UppackarenApplication.getEventBus().post(new StartPollingForProgressEvent());
                UppackarenApplication.getEventBus().post(new StartFetchQueueEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to unrar", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
