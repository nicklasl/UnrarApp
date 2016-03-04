package nu.nldv.uppackaren.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.model.RarArchive;

public class RarArchiveArrayAdapter extends ArrayAdapter<RarArchive> {


    public RarArchiveArrayAdapter(Context context, int resource, List<RarArchive> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_layout, null);
        }

        RarArchive item = getItem(position);
        if (item != null) {
            TextView title = (TextView) view.findViewById(R.id.title_textview);
            TextView right = (TextView) view.findViewById(R.id.number_of_files_textview);
            if(item.isHasSubDirs()) {
                view.findViewById(R.id.dir_imageview).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.dir_imageview).setVisibility(View.INVISIBLE);
            }
            title.setText(item.getName());

            right.setText("~" + item.getDirSizeInMB() + " MB");
        }

        return view;
    }
}
