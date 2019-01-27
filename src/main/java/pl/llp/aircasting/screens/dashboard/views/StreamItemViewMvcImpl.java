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

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionState;

import static pl.llp.aircasting.screens.common.sessionState.ViewingSessionsSensorManager.PLACEHOLDER_SENSOR_NAME;
import static pl.llp.aircasting.screens.dashboard.StreamViewHelper.FIXED_LABEL;
import static pl.llp.aircasting.screens.dashboard.StreamViewHelper.MOBILE_LABEL;

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

    private List<Listener> mListeners = new ArrayList<>();

    private long mSessionId;
    private Sensor mSensor;
    private Session mSession;
    private SessionState mSessionState;
    private int mNowValue;
    private ResourceHelper mResourceHelper;

    public StreamItemViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        mRootView = inflater.inflate(R.layout.stream, parent, false);
        mSessionTitleContainer = findViewById(R.id.title_container);

        mNowTextView = findViewById(R.id.now);
        mLastMeasurementLabel = findViewById(R.id.last_measurement_label);
        mTimestamp = findViewById(R.id.timestamp);
        mSessionTitle = mSessionTitleContainer.findViewById(R.id.session_title);
        mSessionButtonsContainer = mSessionTitleContainer.findViewById(R.id.session_reorder_buttons);

        mPlaceholderChart = findViewById(R.id.placeholder_chart);
        mActualChart = findViewById(R.id.actual_chart);

        getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Listener listener : mListeners) {
                    listener.onStreamClicked(mSensor);
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
    public void bindData(long sessionId, Sensor sensor, Session session, SessionState sessionState, ResourceHelper resourceHelper, int now, Boolean positionWithTitle, Boolean sessionReorderInProgress) {
        mSessionId = sessionId;
        mSensor = sensor;
        mSession = session;
        mResourceHelper = resourceHelper;
        mSessionState = sessionState;
        mNowValue = now;

        if (sensor.getSensorName().startsWith(PLACEHOLDER_SENSOR_NAME)) {
            setTitleView(positionWithTitle, sessionReorderInProgress);
            mPlaceholderChart.setVisibility(View.VISIBLE);
            mActualChart.setVisibility(View.GONE);
            return;
        }

        mLastMeasurementLabel.setText(getLastMeasurementLabel());
        showAndSetTimestamp();

        setTitleView(positionWithTitle, sessionReorderInProgress);

//        mNowTextView.setBackgroundDrawable(getRootView().getContext().getResources().getDrawable(R.drawable.stream_value_grey, null));

        if (mSessionState.sessionHasColoredBackground(sessionId)){
            setBackground(sensor, mNowValue);
        } else {
            mNowTextView.setBackgroundDrawable(mResourceHelper.streamValueGrey);
        }

        if (sessionState.sessionHasNowValue(sessionId)) {
            mNowTextView.setText(String.valueOf(mNowValue));
        } else {
            mNowTextView.setText(R.string.empty);
        }
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    private void setTitleView(Boolean positionWithTitle, Boolean sessionReorderInProgress) {
        if (positionWithTitle) {
            mSessionTitleContainer.setVisibility(View.VISIBLE);
            mSessionTitle.setCompoundDrawablesWithIntrinsicBounds(mSession.getDrawable(), 0, 0, 0);

            if (sessionReorderInProgress) {
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
        if (mSessionState.isSessionCurrent(mSessionId)) {
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

        if (!mSessionState.isSessionCurrent(mSessionId)) {
            Date lastMeasurement = stream.getLastMeasurementTime();
            time = lastMeasurement.getTime();
        } else {
            Calendar calendar = Calendar.getInstance();
            time = calendar.getTime().getTime();
        }

        return dateFormat.format(time);
    }

    private void setBackground(Sensor sensor, double value) {
        mNowTextView.setBackgroundDrawable(mResourceHelper.getStreamValueBackground(sensor, value));
    }
}
