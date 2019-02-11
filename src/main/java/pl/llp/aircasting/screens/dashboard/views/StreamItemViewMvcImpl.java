package pl.llp.aircasting.screens.dashboard.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private final View mActualChart;
    private final TextView mSensorName;
    private final TextView mMoveSessionDown;
    private final TextView mMoveSessionUp;

    private List<Listener> mListeners = new ArrayList<>();

    private Sensor mSensor;
    private Session mSession;
    private String mNowValue = "";
    private ResourceHelper mResourceHelper;
    private Boolean mSessionReorderInProgress = false;
    private String mSensorNameText;
    private Boolean mSessionCurrent;
    private boolean mHasColorBackground;
    private boolean mNowValuePresent;
    private long mSessionId;

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
        mActualChart = findViewById(R.id.actual_chart);

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
    public void bindData(Map<String, Object> dataItem, Boolean positionWithTitle, ResourceHelper resourceHelper) {
        mSensor = (Sensor) dataItem.get(SENSOR);
        mSensorNameText = mSensor.getSensorName();
        mSession = (Session) dataItem.get(SESSION);
        mSessionId = (long) dataItem.get(SESSION_ID);
        mSessionCurrent = (Boolean) dataItem.get(SESSION_CURRENT);
//        mNowValuePresent = (Boolean) dataItem.get(NOW_VALUE_PRESENT);
        mHasColorBackground = (Boolean) dataItem.get(BACKGROUND_COLOR);
        mNowValue = String.valueOf(dataItem.get(NOW_VALUE));
        mSessionReorderInProgress = (Boolean) dataItem.get(REORDER_IN_PROGRESS);
        mResourceHelper = resourceHelper;

        drawFullView(positionWithTitle);
    }

    public void bindNowValue(double nowValue) {
        mNowValue = String.valueOf(nowValue);
        mNowTextView.setText(mNowValue);
    }

    private void drawFullView(Boolean positionWithTitle) {
        if (mSensorNameText.startsWith(PLACEHOLDER_SENSOR_NAME)) {
            setTitleView(positionWithTitle);
            mPlaceholderChart.setVisibility(View.VISIBLE);
            mActualChart.setVisibility(View.GONE);
            return;
        }

        mRootView.setTag(R.id.session_id_tag, mSessionId);

        mLastMeasurementLabel.setText(getLastMeasurementLabel());
        showAndSetTimestamp();

        setTitleView(positionWithTitle);

        mSensorName.setText(mSensorNameText);

        if (mHasColorBackground) {
            setBackground();
        } else {
            mNowTextView.setBackgroundDrawable(mResourceHelper.streamValueGrey);
        }

//        if (mNowValuePresent) {
            mNowTextView.setText(mNowValue);
//        } else {
//            mNowTextView.setText(R.string.empty);
//        }

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

    private void setTitleView(Boolean positionWithTitle) {
        if (positionWithTitle) {
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

    private String getLastMeasurementLabel() {
        if (mSession.isFixed()) {
            return FIXED_LABEL;
        } else {
            return MOBILE_LABEL;
        }
    }

    private void showAndSetTimestamp() {
        if (mSessionCurrent) {
            mTimestamp.setVisibility(View.GONE);
        } else {
            mTimestamp.setVisibility(View.VISIBLE);
            mTimestamp.setText(getTimestamp());
        }
    }

    private String getTimestamp() {
        double time;
        MeasurementStream stream = mSession.getStream(mSensor.getSensorName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm MM/dd/yy");

        if (mSessionCurrent) {
            Date lastMeasurement = stream.getLastMeasurementTime();
            time = lastMeasurement.getTime();
        } else {
            Calendar calendar = Calendar.getInstance();
            time = calendar.getTime().getTime();
        }

        return dateFormat.format(time);
    }

    private void setBackground() {
        mNowTextView.setBackgroundDrawable(mResourceHelper.getStreamValueBackground(mSensor, Double.parseDouble(mNowValue)));
    }
}
