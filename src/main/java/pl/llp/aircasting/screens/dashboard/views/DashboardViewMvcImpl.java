package pl.llp.aircasting.screens.dashboard.views;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.dashboard.adapters.CurrentStreamsRecyclerAdapter;
import pl.llp.aircasting.screens.dashboard.adapters.ViewingStreamsRecyclerAdapter;
import pl.llp.aircasting.screens.dashboard.helper.StreamItemTouchHelperCallback;

public class DashboardViewMvcImpl implements DashboardViewMvc, View.OnClickListener, StreamItemViewMvc.Listener {
    private final View mRootView;
    private final LayoutInflater mInflater;
    private final View mEmptyLayout;
    private final RecyclerView mCurrentStreamsRecyclerView;
    private final RecyclerView mViewingStreamsRecyclerView;
    private final View mMicrophoneButton;
    private final View mSensorsButton;
    private final View mAirbeam2ConfigButton;
    private final CurrentStreamsRecyclerAdapter mCurrentRecyclerAdapter;
    private final ViewingStreamsRecyclerAdapter mViewingRecyclerAdapter;
    private final AppCompatActivity mContext;

    private Listener mListener;

    public DashboardViewMvcImpl(AppCompatActivity context, ViewGroup parent, ResourceHelper resourceHelper) {
        mContext = context;
        mInflater = mContext.getLayoutInflater();
        mRootView = mInflater.inflate(R.layout.dashboard, parent, false);

        mEmptyLayout = findViewById(R.id.layout_empty);
        mCurrentStreamsRecyclerView = findViewById(R.id.current_streams_recycler_view);
        mViewingStreamsRecyclerView = findViewById(R.id.viewing_streams_recycler_view);
        mMicrophoneButton = findViewById(R.id.dashboard_microphone);
        mSensorsButton = findViewById(R.id.dashboard_sensors);
        mAirbeam2ConfigButton = findViewById(R.id.configure_airbeam2);

        if (mMicrophoneButton != null) { mMicrophoneButton.setOnClickListener(this); }
        if (mSensorsButton != null) { mSensorsButton.setOnClickListener(this); }
        if (mAirbeam2ConfigButton != null) { mAirbeam2ConfigButton.setOnClickListener(this); }

        mCurrentRecyclerAdapter = new CurrentStreamsRecyclerAdapter(mInflater, this, resourceHelper);
        RecyclerView.LayoutManager currentLayoutManager = new LinearLayoutManager(mContext);
        mCurrentStreamsRecyclerView.setLayoutManager(currentLayoutManager);
        mCurrentStreamsRecyclerView.setAdapter(mCurrentRecyclerAdapter);
        mCurrentStreamsRecyclerView.setNestedScrollingEnabled(false);

        mViewingRecyclerAdapter = new ViewingStreamsRecyclerAdapter(mInflater, this, resourceHelper);
        RecyclerView.LayoutManager viewingLayoutManager = new LinearLayoutManager(mContext);
        mViewingStreamsRecyclerView.setLayoutManager(viewingLayoutManager);
        mViewingStreamsRecyclerView.setAdapter(mViewingRecyclerAdapter);
        mViewingStreamsRecyclerView.setNestedScrollingEnabled(false);

        ItemTouchHelper.Callback callback = new StreamItemTouchHelperCallback(mCurrentRecyclerAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mCurrentStreamsRecyclerView);

        ItemTouchHelper.Callback callback1 = new StreamItemTouchHelperCallback(mViewingRecyclerAdapter);
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(callback1);
        itemTouchHelper1.attachToRecyclerView(mViewingStreamsRecyclerView);
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public void registerListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void unregisterListener() {
        mListener = null;
    }

    @Override
    public void bindSensorData(List data) {
        if (!data.isEmpty()) {
            mEmptyLayout.setVisibility(View.GONE);
        } else if (mViewingRecyclerAdapter.getItemCount() == 0){
            mEmptyLayout.setVisibility(View.VISIBLE);
        }

        mCurrentRecyclerAdapter.bindData(data);
    }

    @Override
    public void bindRecentMeasurements(Map<String, Double> recentMeasurementsData) {
        mCurrentRecyclerAdapter.bindNowValues(recentMeasurementsData);
    }

    @Override
    public void bindRecentFixedMeasurements(Map<String, Measurement> recentFixedMeasurements) {
        Log.w("dashb view bind", String.valueOf(recentFixedMeasurements));
        mViewingRecyclerAdapter.bindRecentFixedMeasurements(recentFixedMeasurements);
    }

    @Override
    public void bindChartData(Map liveCharts) {
        mCurrentRecyclerAdapter.bindChartData(liveCharts);
    }

    @Override
    public void bindViewingSensorsData(List data) {
        if (!data.isEmpty()) {
            mEmptyLayout.setVisibility(View.GONE);
            mViewingStreamsRecyclerView.setVisibility(View.VISIBLE);
        } else if (mCurrentRecyclerAdapter.getItemCount() == 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mViewingStreamsRecyclerView.setVisibility(View.GONE);
        }

        mViewingRecyclerAdapter.bindData(data);
    }

    @Override
    public void bindStaticChartData(Map staticCharts) {
        mViewingRecyclerAdapter.bindChartData(staticCharts);
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    @Override
    public void onClick(View v) {
        mListener.onDashboardButtonClicked(v);
    }

    @Override
    public void onStreamClicked(View view) {
        mListener.onStreamClicked(view);
    }

    @Override
    public void onSessionUpClicked(View view, long sessionId) {}

    @Override
    public void onSessionDownClicked(View view, long sessionId) {}
}
