package pl.llp.aircasting.screens.dashboard.views;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;

import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.BACKGROUND_COLOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.REORDER_IN_PROGRESS;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SENSOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_ID;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_RECORDING;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.STREAM_CHART;
import static pl.llp.aircasting.util.Constants.MOBILE_LABEL;

public class CurrentStreamItemViewMvcImpl implements StreamItemViewMvc {
    private final View mRootView;

    private final RelativeLayout mSessionTitleContainer;
    private final TextView mNowTextView;
    private final TextView mLastMeasurementLabel;
    private final TextView mTimestamp;
    private final TextView mSessionTitle;
    private final TextView mSensorName;
    private final RelativeLayout mChartLayout;

    private List<Listener> mListeners = new ArrayList<>();

    private Sensor mSensor;
    private Session mSession;
    private String mNowValue = "0";
    private ResourceHelper mResourceHelper;
    private String mSensorNameText;
    private Boolean mSessionRecording;
    private long mSessionId;
    private int mPosition;
    private LineChart mChart;

    public CurrentStreamItemViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        mRootView = inflater.inflate(R.layout.stream_row, parent, false);
        mSessionTitleContainer = findViewById(R.id.title_container);

        mSensorName = findViewById(R.id.sensor_name);
        mNowTextView = findViewById(R.id.now);
        mLastMeasurementLabel = findViewById(R.id.last_measurement_label);
        mTimestamp = findViewById(R.id.timestamp);
        mSessionTitle = mSessionTitleContainer.findViewById(R.id.session_title);

        mChartLayout = findViewById(R.id.chart_layout);

        getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Listener listener : mListeners) {
                    listener.onStreamClicked(v);
                }
            }
        });
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void bindData(Map<String, Object> dataItem, int position, ResourceHelper resourceHelper) {
        mPosition = position;
        mSensor = (Sensor) dataItem.get(SENSOR);
        mSensorNameText = mSensor.getSensorName();
        mSession = (Session) dataItem.get(SESSION);
        mSessionId = (long) dataItem.get(SESSION_ID);
        mSessionRecording = (Boolean) dataItem.get(SESSION_RECORDING);
        mChart = (LineChart) dataItem.get(STREAM_CHART);
        mResourceHelper = resourceHelper;

        drawFullView();
    }

    @Override
    public void bindNowValue(Map<String, Double> nowValues) {
        Number now = nowValues.get(mSensorNameText);

        if (now != null) {
            mNowValue = String.valueOf(now.intValue());
            mNowTextView.setText(mNowValue);

            setBackground();
        } else {
            mNowTextView.setText(mNowValue);
        }
    }

    @Override
    public void bindRecentFixedMeasurement(Map<String, Measurement> recentFixedMeasurements) {}

    @Override
    public void bindChart(Map mChartData) {
        mChart = (LineChart) mChartData.get(mSensorNameText);

        if (mChart != null) {
            mChartLayout.removeAllViews();
            mChartLayout.addView(mChart, mChartLayout.getWidth(), mChartLayout.getHeight());
        }
    }

    @Override
    public void bindSessionTitle(int position) {
        mPosition = position;
        setTitleView();
    }

    private void drawFullView() {
        mRootView.setTag(R.id.session_id_tag, mSessionId);

        mLastMeasurementLabel.setText(MOBILE_LABEL);
        mTimestamp.setVisibility(View.GONE);

        setTitleView();

        mSensorName.setText(mSensorNameText);

        setBackground();

        mNowTextView.setText(mNowValue);
        if (mChart.getParent() != null) {
            ((ViewGroup) mChart.getParent()).removeView(mChart);
        }
        mChartLayout.removeAllViews();
        mChartLayout.addView(mChart, mChartLayout.getWidth(), mChartLayout.getHeight());
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    private void setTitleView() {
        if (positionWithTitle()) {

            mSessionTitleContainer.setVisibility(View.VISIBLE);
            mSessionTitle.setCompoundDrawablesWithIntrinsicBounds(mSession.getDrawable(), 0, 0, 0);

            if (!mSession.hasTitle()) {
                mSessionTitle.setText(R.string.unnamed);
            } else {
                mSessionTitle.setText(mSession.getTitle());
            }
        } else {
            mSessionTitleContainer.setVisibility(View.GONE);
        }
    }

    private boolean positionWithTitle() {
        return mPosition == 0;
    }

    private void setBackground() {
        if (mSessionRecording) {
            mNowTextView.setBackground(mResourceHelper.getStreamValueBackground(mSensor, Double.parseDouble(mNowValue)));
        } else {
            mNowTextView.setBackgroundDrawable(mResourceHelper.streamValueGrey);
        }
    }
}
