package pl.llp.aircasting.helper;

import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class TopBarHelperTest {
    @Inject TopBarHelper helper;

    @Mock ThresholdsHelper thresholdsHelper;
    @Mock Sensor sensor;
    @Mock View parent;
    @Mock TextView veryLow;
    @Mock TextView low;
    @Mock TextView mid;
    @Mock TextView high;
    @Mock TextView veryHigh;

    @Before
    public void setup() {
        helper.thresholdsHelper = thresholdsHelper;

        when(thresholdsHelper.getThreshold(sensor, MeasurementLevel.VERY_LOW)).thenReturn(0);
        when(thresholdsHelper.getThreshold(sensor, MeasurementLevel.LOW)).thenReturn(10);
        when(thresholdsHelper.getThreshold(sensor, MeasurementLevel.MID)).thenReturn(20);
        when(thresholdsHelper.getThreshold(sensor, MeasurementLevel.HIGH)).thenReturn(30);
        when(thresholdsHelper.getThreshold(sensor, MeasurementLevel.VERY_HIGH)).thenReturn(40);
        
        when(parent.findViewById(R.id.top_bar_very_low)).thenReturn(veryLow);
        when(parent.findViewById(R.id.top_bar_low)).thenReturn(low);
        when(parent.findViewById(R.id.top_bar_mid)).thenReturn(mid);
        when(parent.findViewById(R.id.top_bar_high)).thenReturn(high);
        when(parent.findViewById(R.id.top_bar_very_high)).thenReturn(veryHigh);

        helper.updateTopBar(sensor, parent);
    }
    
    @Test
    public void shouldFillVeryLow(){
        verify(veryLow).setText("0");
    }
    
    @Test
    public void shouldFillLow(){
        verify(low).setText("10");
    }

    @Test
    public void shouldFillMid(){
        verify(mid).setText("20");
    }

    @Test
    public void shouldFillHigh(){
        verify(high).setText("30");
    }

    @Test
    public void shouldFillVeryHigh(){
        verify(veryHigh).setText("40");
    }
}
