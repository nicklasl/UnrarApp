package nu.nldv.uppackaren;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nu.nldv.uppackaren.model.RarArchive;

public class RarArchiveArrayAdapter extends ArrayAdapter<RarArchive> {

    private final Context context;
    private final List<RarArchive> items;

    public RarArchiveArrayAdapter(Context context, int resource, List<RarArchive> items) {
        super(context, resource);
        this.context = context;
        this.items=items;
    }

    @Override
    public void addAll(Collection<? extends RarArchive> collection) {
        this.items.addAll(collection);
    }

    @Override
    public void addAll(RarArchive... items) {
        this.items.addAll(Arrays.asList(items));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_layout, null);
        }

        RarArchive item = items.get(position);
        if (item != null) {
            TextView title = (TextView) view.findViewById(R.id.title_textview);
            TextView right = (TextView) view.findViewById(R.id.number_of_files_textview);
            title.setText(item.getName());

            right.setText("~"+item.getDirSizeInMB()+" MB");
        }

        return view;
    }
}
