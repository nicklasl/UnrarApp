package nu.nldv.uppackaren.view;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nu.nldv.uppackaren.MainActivity;
import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.UppackarenApplication;
import nu.nldv.uppackaren.event.ReloadArchivesEvent;
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
    private static final String ARG_ARCHIVE = "arg_archive";
    private RarArchive archive;

    @InjectView(R.id.listview)
    ListView listView;

    private List<RarArchive> list;
    private RarArchiveArrayAdapter adapter;
    private View loader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            final Serializable serializable = getArguments().getSerializable(ARG_ARCHIVE);
            if (serializable != null && serializable instanceof RarArchive) {
                archive = (RarArchive) serializable;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_archives, container, false);
        loader = view.findViewById(R.id.loader);
        View upButton = view.findViewById(R.id.go_up_imageview);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        if (archive == null) {
            upButton.setVisibility(View.GONE);
        } else {
            upButton.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter = new RarArchiveArrayAdapter(getActivity(), R.layout.row_layout, new ArrayList<RarArchive>());
        adapter.clear();
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        loadData();
    }

    public void loadData() {
        String id = "";
        if (archive != null) {
            id = archive.getId();
        }
        loader.setVisibility(View.VISIBLE);
        getRestAPI().getRarArchives(id, new Callback<List<RarArchive>>() {
            @Override
            public void success(List<RarArchive> rarArchives, Response response) {
                if (isAdded()) {
                    loader.setVisibility(View.GONE);
                    list = rarArchives;
                    adapter.clear();
                    adapter.addAll(rarArchives);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        final RarArchive rarArchive = list.get(position);
        if (rarArchive.isHasSubDirs()) {
            reloadListViewWithSubDir(rarArchive);
        } else {
            initiateUnrar(rarArchive, position);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listview) {
            menu.setHeaderTitle(R.string.action);
            menu.add(Menu.NONE, 0, 0, R.string.unrar);
            menu.add(Menu.NONE, 1, 1, R.string.navigate);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final int listPosition = info.position;
        final RarArchive rarArchive = list.get(listPosition);
        switch (item.getItemId()) {
            case 0:
                initiateUnrar(rarArchive, listPosition);
                return true;
            case 1:
                reloadListViewWithSubDir(rarArchive);
                return true;
            default:
                return false;
        }


    }

    @Subscribe
    public void loadEventReceived(ReloadArchivesEvent event) {
        if(isAdded()) {
            loadData();
        }
    }

    private void reloadListViewWithSubDir(RarArchive rarArchive) {
        ((MainActivity) getActivity()).pushFragment(rarArchive);
    }

    public static ArchivesFragment newInstance(RarArchive rarArchive) {
        ArchivesFragment archivesFragment = new ArchivesFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ARCHIVE, rarArchive);
        archivesFragment.setArguments(bundle);
        return archivesFragment;
    }

    private void initiateUnrar(final RarArchive rarArchive, final int position) {
        getRestAPI().unRar(rarArchive.getId(), new Callback<UnrarResponse>() {
            @Override
            public void success(UnrarResponse unrarResponse, Response response) {
                if (isAdded()) {
                    list.remove(position);
                    adapter.remove(rarArchive);
                    adapter.notifyDataSetChanged();
                    UppackarenApplication.getEventBus().post(new StartPollingForProgressEvent());
                    UppackarenApplication.getEventBus().post(new StartFetchQueueEvent());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to unrar", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
