package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;

import android.view.View;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by ags on 26/07/12 at 17:10
 */
public class AdapterInteractorTest
{
  private SettingsHelper settings;
  private AdapterInteractor interactor;
  private ConnectedSensorAdapter connected;
  private PairedSensorAdapter paired;
  private ExternalSensorDescriptor descriptor;

  @Before
  public void setUp() throws Exception
  {
    settings = mock(SettingsHelper.class);
    connected = mock(ConnectedSensorAdapter.class);
    paired = mock(PairedSensorAdapter.class);
    ExternalSensorActivity activity = mock(ExternalSensorActivity.class);
    View mock = mock(View.class);
    when(activity.getDelegate().findViewById(anyInt())).thenReturn(mock);
    interactor = new AdapterInteractor(activity,
                                       paired,
                                       connected,
                                       settings);
    descriptor = mock(ExternalSensorDescriptor.class);
  }

  @Test
  public void should_connectToActive() throws Exception
  {
      // given

      // when
    when(paired.get(0)).thenReturn(descriptor);
    interactor.connectToActive(0);

      // then
    verify(descriptor).setAction("disconnect");
    verify(connected).addSensor(any(ExternalSensorDescriptor.class));
  }
  
  @Test
  public void should_disconnect() throws Exception
  {
      // given

      // when
    interactor.disconnect(0);
      
      // then
    verify(connected).remove(anyInt());
    verify(connected).updatePreviouslyConnected(anyList());
  }
}
