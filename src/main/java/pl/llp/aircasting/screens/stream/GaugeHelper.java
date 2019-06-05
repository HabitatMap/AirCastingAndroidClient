package pl.llp.aircasting.screens.stream;

import pl.llp.aircasting.MarkerSize;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionDataFactory;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;

import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import roboguice.inject.InjectResource;

import static java.lang.String.valueOf;

@Singleton
public class GaugeHelper {
    public static final int MARGIN = 2;

    private static String AVG_LABEL = "Avg %s";
    private static String NOW_LABEL = "Now %s";
    private static String PEAK_LABEL = "Peak %s";

    private final View mNowContainer;
    private final TextView mNowGauge;
    private final TextView mAvgGauge;
    private final TextView mPeakGauge;
    private final TextView mNowTextView;
    private final TextView mAvgTextView;
    private final TextView mPeakTextView;

    private ResourceHelper mResourceHelper;
    private VisibleSession mVisibleSession;
    private SessionDataFactory mSessionData;

//    @InjectResource(R.string.avg_label_template) String avgLabel;
//    @InjectResource(R.string.now_label_template) String nowLabel;
//    @InjectResource(R.string.peak_label_template) String peakLabel;

    private double mPeak;
    private double mAverage;

    public GaugeHelper(View view,
                       ResourceHelper resourceHelper,
                       VisibleSession visibleSession,
                       SessionDataFactory sessionDataFactory) {
        mResourceHelper = resourceHelper;
        mVisibleSession = visibleSession;
        mSessionData = sessionDataFactory;

        mNowContainer = view.findViewById(R.id.now_container);
        mNowGauge = view.findViewById(R.id.now_gauge);
        mAvgGauge = view.findViewById(R.id.avg_gauge);
        mPeakGauge = view.findViewById(R.id.peak_gauge);

        mNowTextView = view.findViewById(R.id.now_label);
        mAvgTextView = view.findViewById(R.id.avg_label);
        mPeakTextView = view.findViewById(R.id.peak_label);
    }

    public void updateGauges() {
        Sensor sensor = mVisibleSession.getSensor();
        toggleNowContainerVisibility();

        int now = (int) mSessionData.getNow(sensor, mVisibleSession.getVisibleSessionId());
        updateGauge(mNowGauge, sensor, MarkerSize.BIG, now);

        String nowText = String.format(NOW_LABEL, sensor.getShortType());
        String avgText = String.format(AVG_LABEL, sensor.getShortType());
        String peakText = String.format(PEAK_LABEL, sensor.getShortType());

        float avgSize = findMinimumVisibleSize(mAvgTextView, avgText);
        float peakSize = findMinimumVisibleSize(mPeakTextView, peakText);
        float nowSize = findMinimumVisibleSize(mNowTextView, nowText);

        avgSize = peakSize = Math.min(avgSize, peakSize);

        updateLabel(mNowTextView, nowText, nowSize);
        updateLabel(mAvgTextView, avgText, avgSize);
        updateLabel(mPeakTextView, peakText, peakSize);

        boolean hasStats = mVisibleSession.isVisibleSessionRecording() || mVisibleSession.isVisibleSessionViewed();

        if (hasStats) {
            int avg = (int) mVisibleSession.getAvg(sensor);
            int peak = (int) mVisibleSession.getPeak(sensor);

            updateGauge(mAvgGauge, sensor, MarkerSize.SMALL, avg);
            updateGauge(mPeakGauge, sensor, MarkerSize.SMALL, peak);
        } else {
            displayInactiveGauge(mAvgGauge, MarkerSize.SMALL);
            displayInactiveGauge(mPeakGauge, MarkerSize.SMALL);
        }
    }

    private void updateLabel(TextView view, String label, float size) {
        view.getPaint().setTextSize(size);
        view.setText(label);
    }

    private float findMinimumVisibleSize(TextView textView, String message) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        long viewWidth = textView.getWidth();

        if (viewWidth < 1) {
            textView.measure(0, 0);
            viewWidth = textView.getMeasuredWidth();

            if (viewWidth < 1) {
                return textView.getTextSize();
            }
        }

        TextPaint textPaint = textView.getPaint();
        float textSize = textView.getTextSize();
        float paintWidth = textPaint.measureText(message) + 2 * MARGIN;

        while (paintWidth > viewWidth) {
            textSize--;
            textPaint.setTextSize(textSize);
            paintWidth = textPaint.measureText(message) + 2 * MARGIN;
        }

        return textSize;
    }

    private void updateGauge(View view, Sensor sensor, MarkerSize size, int value) {
        TextView textView = (TextView) view;

        textView.setText(valueOf(value));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mResourceHelper.getTextSize(value, size));

        if (mVisibleSession.isVisibleSessionRecording() || mVisibleSession.isVisibleSessionViewed()) {
            textView.setBackgroundDrawable(mResourceHelper.getGauge(sensor, size, value));
        } else {
            textView.setBackgroundDrawable(mResourceHelper.getDisabledGauge(size));
        }
    }

    private void displayInactiveGauge(View view, MarkerSize size) {
        TextView textView = (TextView) view;

        textView.setText("--");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, ResourceHelper.SMALL_GAUGE_SMALL_TEXT);
        textView.setBackgroundDrawable(mResourceHelper.getDisabledGauge(size));
    }

    public void setDynamicValues(double peak, double avg) {
        mPeak = peak;
        mAverage = avg;
    }

    private void toggleNowContainerVisibility() {
        if (mVisibleSession.isVisibleSessionFixed() || mVisibleSession.isCurrentSessionVisible()) {
            mNowContainer.setVisibility(View.VISIBLE);
        } else {
            mNowContainer.setVisibility(View.GONE);
        }
    }
}
