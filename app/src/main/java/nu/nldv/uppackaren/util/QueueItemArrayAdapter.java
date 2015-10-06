package nu.nldv.uppackaren.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.model.QueueItem;

public class QueueItemArrayAdapter extends ArrayAdapter<QueueItem> {

    public QueueItemArrayAdapter(Context context, int rowResource, List<QueueItem> items) {
        super(context, rowResource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.queue_row_layout, null);
        }

        QueueItem item = getItem(position);
        if (item != null) {
            TextView title = (TextView) view.findViewById(R.id.queue_item_title);
            title.setText(item.getDir());
        }

        return view;
    }
}
