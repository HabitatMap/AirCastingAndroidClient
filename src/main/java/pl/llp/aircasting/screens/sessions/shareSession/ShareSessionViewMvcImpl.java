package pl.llp.aircasting.screens.sessions.shareSession;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pl.llp.aircasting.R;
import pl.llp.aircasting.common.BaseViewMvc;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;

public class ShareSessionViewMvcImpl implements BaseViewMvc, View.OnClickListener, SensorItemViewMvcImpl.Listener {
    private final SensorRecyclerAdapter mSensorAdapter;
    private final View mButtons;
    private final Toolbar mToolbar;
    public View mRootView;

    private final View mShareFile;
    private final View mShareLink;
    private final RecyclerView mSelectSensorRv;
    private View.OnClickListener mClickListener;

    private Session mSession;
    private Context mContext;
    private SensorItemViewMvcImpl.Listener mSensorClickListener;

    public ShareSessionViewMvcImpl(AppCompatActivity context, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        mContext = context;
        mRootView = inflater.inflate(R.layout.share_session, parent, false);
        mButtons = findViewById(R.id.share_buttons);
        mShareFile = findViewById(R.id.share_file);
        mShareLink = findViewById(R.id.share_link);
        mSelectSensorRv = findViewById(R.id.select_sensor);
        mToolbar = findViewById(R.id.toolbar);

        mSensorAdapter = new SensorRecyclerAdapter(inflater);
        mSensorAdapter.registerListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mSelectSensorRv.setLayoutManager(layoutManager);
        mSelectSensorRv.setAdapter(mSensorAdapter);

        mShareFile.setOnClickListener(this);
        mShareLink.setOnClickListener(this);
    }

    public void registerListener(View.OnClickListener listener) {
        mClickListener = listener;
    }

    public void registerListener(SensorItemViewMvcImpl.Listener listener) {
        mSensorClickListener = listener;
    }

    public void bindData(Session session) {
        mSession = session;
        List<String> sensorNames = new ArrayList<>();

        for (MeasurementStream stream : session.getMeasurementStreams()) {
            sensorNames.add(stream.getSensorName());
        }
        mSensorAdapter.bindData(sensorNames);
    }

    public void toggleLink(boolean isLocationless) {
        if (isLocationless) {
            mShareLink.setVisibility(View.GONE);
        } else {
            mShareLink.setVisibility(View.VISIBLE);
        }
    }

    public void showSensors() {
        mButtons.setVisibility(View.GONE);
        mSelectSensorRv.setVisibility(View.VISIBLE);
        mToolbar.setTitle("Select a Stream");
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    @Override
    public void onClick(View v) {
        mClickListener.onClick(v);
    }

    @Override
    public void onSensorSelected(View view) {
        mSensorClickListener.onSensorSelected(view);
    }
}
