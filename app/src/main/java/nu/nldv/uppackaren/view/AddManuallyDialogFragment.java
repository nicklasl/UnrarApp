package nu.nldv.uppackaren.view;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import nu.nldv.uppackaren.R;
import nu.nldv.uppackaren.UppackarenApplication;
import roboguice.fragment.provided.RoboDialogFragment;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

public class AddManuallyDialogFragment extends RoboDialogFragment implements View.OnClickListener {

    public static final String TAG = AddManuallyDialogFragment.class.getSimpleName();
    private Handler handler;

    @InjectView(R.id.search_for_server_add_manually_edittext)
    EditText manualEditText;

    @InjectView(R.id.search_for_server_add_manually_ok_button)
    Button okButton;

    public static AddManuallyDialogFragment newInstance(Handler handler) {
        AddManuallyDialogFragment fragment = new AddManuallyDialogFragment();
        fragment.setHandler(handler);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.add_manually);
        return inflater.inflate(R.layout.add_manually_container, container, false);
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        okButton.setOnClickListener(this);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(UppackarenApplication.UPPACKAREN, Activity.MODE_PRIVATE);
        if (sharedPreferences.contains(UppackarenApplication.UPPACKAREN_SERVER_URI)) {
            manualEditText.setText(sharedPreferences.getString(UppackarenApplication.UPPACKAREN_SERVER_URI, "not set"));
        }
    }

    private void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onClick(View v) {
        String result = manualEditText.getText().toString();
        handler.handleOk(result);
    }

    public interface Handler {
        void handleOk(String result);
    }
}
