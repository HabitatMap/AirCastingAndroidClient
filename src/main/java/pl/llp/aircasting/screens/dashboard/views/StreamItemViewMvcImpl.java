package pl.llp.aircasting.screens.dashboard.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionState;

import static pl.llp.aircasting.screens.common.sessionState.ViewingSessionsSensorManager.PLACEHOLDER_SENSOR_NAME;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.BACKGROUND_COLOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.NOW_VALUE;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.REORDER_IN_PROGRESS;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SENSOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_CURRENT;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_ID;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_RECORDING;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.STREAM_CHART;
import static pl.llp.aircasting.util.Constants.FIXED_LABEL;
import static pl.llp.aircasting.util.Constants.MOBILE_LABEL;

public class StreamItemViewMvcImpl implements StreamItemViewMvc {
    private final View mRootView;

    private final RelativeLayout mSessionTitleContainer;
    private final TextView mNowTextView;
    private final TextView mLastMeasurementLabel;
    private final TextView mTimestamp;
    private final TextView mSessionTitle;
    private final LinearLayout mSessionButtonsContainer;
    private final View mPlaceholderChart;
    private final TextView mSensorName;
    private final TextView mMoveSessionDown;
    private final TextView mMoveSessionUp;
    private final RelativeLayout mChartLayout;

    private List<Listener> mListeners = new ArrayList<>();

    private Sensor mSensor;
    private Session mSession;
    private String mNowValue = "";
    private ResourceHelper mResourceHelper;
    private Boolean mSessionReorderInProgress = false;
    private String mSensorNameText;
    private Boolean mSessionRecording;
    private boolean mHasColorBackground;
    private long mSessionId;
    private int mPosition;
    private LineChart mChart;

    public StreamItemViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        mRootView = inflater.inflate(R.layout.stream_row, parent, false);
        mSessionTitleContainer = findViewById(R.id.title_container);

        mSensorName = findViewById(R.id.sensor_name);
        mNowTextView = findViewById(R.id.now);
        mLastMeasurementLabel = findViewById(R.id.last_measurement_label);
        mTimestamp = findViewById(R.id.timestamp);
        mSessionTitle = mSessionTitleContainer.findViewById(R.id.session_title);
        mSessionButtonsContainer = mSessionTitleContainer.findViewById(R.id.session_reorder_buttons);
        mMoveSessionUp = mSessionButtonsContainer.findViewById(R.id.session_up);
        mMoveSessionDown = mSessionButtonsContainer.findViewById(R.id.session_down);

        mPlaceholderChart = findViewById(R.id.placeholder_chart);
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
        mHasColorBackground = (Boolean) dataItem.get(BACKGROUND_COLOR);
        mNowValue = String.valueOf(dataItem.get(NOW_VALUE));
        mSessionReorderInProgress = (Boolean) dataItem.get(REORDER_IN_PROGRESS);
        mChart = (LineChart) dataItem.get(STREAM_CHART);
        mResourceHelper = resourceHelper;

        drawFullView();
    }

    @Override
    public void bindNowValue(TreeMap<String, Double> nowValues) {
        Number now = nowValues.get(mSensorNameText);

        if (now != null) {
            mNowTextView.setText(String.valueOf(now.intValue()));

            if (mSessionRecording) {
                mNowTextView.setBackground(mResourceHelper.getStreamValueBackground(mSensor, now.doubleValue()));
            }
        } else {
            mNowTextView.setText("0");
        }
    }

    @Override
    public void bindChart(Map mChartData) {
        mChart = (LineChart) mChartData.get(mSensorNameText);
        Log.w("chart to bind", String.valueOf(mChart));

        if (mChart != null) {
            mChartLayout.removeAllViews();
            mChartLayout.addView(mChart, mChartLayout.getWidth(), mChartLayout.getHeight());
        }
    }

    private void drawFullView() {
        Log.w("stream item", "draw full view");
//        if (mSensorNameText.startsWith(PLACEHOLDER_SENSOR_NAME)) {
//            setTitleView();
//            mPlaceholderChart.setVisibility(View.VISIBLE);
//            mActualChart.setVisibility(View.GONE);
//            return;
//        }

        mRootView.setTag(R.id.session_id_tag, mSessionId);

        mLastMeasurementLabel.setText(getLastMeasurementLabel());
        showAndSetTimestamp();

        setTitleView();

        mSensorName.setText(mSensorNameText);

        if (mHasColorBackground) {
            setBackground();
        } else {
            mNowTextView.setBackgroundDrawable(mResourceHelper.streamValueGrey);
        }

        mNowTextView.setText(mNowValue);
        mChartLayout.removeAllViews();
        mChartLayout.addView(mChart, mChartLayout.getWidth(), mChartLayout.getHeight());

        if (mSessionReorderInProgress) {
            mMoveSessionDown.setVisibility(View.VISIBLE);
            mMoveSessionUp.setVisibility(View.VISIBLE);

            mMoveSessionDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    private void setTitleView() {
        if (positionWithTitle()) {
            mSessionTitleContainer.setVisibility(View.VISIBLE);
            mSessionTitle.setCompoundDrawablesWithIntrinsicBounds(mSession.getDrawable(), 0, 0, 0);

            if (mSessionReorderInProgress) {
                mSessionButtonsContainer.setVisibility(View.VISIBLE);
            } else {
                mSessionButtonsContainer.setVisibility(View.GONE);
            }

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

    private String getLastMeasurementLabel() {
//        if (mSession.isFixed()) {
//            return FIXED_LABEL;
//        } else {
            return MOBILE_LABEL;
//        }
    }

    private void showAndSetTimestamp() {
//        if (mSessionCurrent) {
            mTimestamp.setVisibility(View.GONE);
//        } else {
//            mTimestamp.setVisibility(View.VISIBLE);
//            mTimestamp.setText(getTimestamp());
//        }
    }

//    private String getTimestamp() {
//        double time;
//        MeasurementStream stream = mSession.getStream(mSensor.getSensorName());
//        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm MM/dd/yy");
//
//        if (mSessionCurrent) {
//            Date lastMeasurement = stream.getLastMeasurementTime();
//            time = lastMeasurement.getTime();
//        } else {
//            Calendar calendar = Calendar.getInstance();
//            time = calendar.getTime().getTime();
//        }
//
//        return dateFormat.format(time);
//    }

    private void setBackground() {
        mNowTextView.setBackgroundDrawable(mResourceHelper.getStreamValueBackground(mSensor, Double.parseDouble(mNowValue)));
    }
}
