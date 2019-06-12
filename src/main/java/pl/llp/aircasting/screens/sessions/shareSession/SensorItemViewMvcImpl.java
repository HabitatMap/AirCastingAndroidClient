package pl.llp.aircasting.screens.sessions.shareSession;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pl.llp.aircasting.R;
import pl.llp.aircasting.common.BaseViewMvc;

public class SensorItemViewMvcImpl implements BaseViewMvc {
    private final Button mSensorName;
    private View mRootView;
    private Listener mListener;

    public interface Listener {
        void onSensorSelected(View view);
    }

    public SensorItemViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        mRootView = inflater.inflate(R.layout.sensor_item, parent, false);
        mSensorName = findViewById(R.id.sensor_name);

        mSensorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSensorSelected(v);
            }
        });
    }

    public void registerListener(Listener listener) {
        mListener = listener;
    }

    public void bindData(String sensorName) {
        mSensorName.setText(sensorName);
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }
}
