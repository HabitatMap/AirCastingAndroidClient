package pl.llp.aircasting.service;

import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.helper.SoundHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;

import android.content.Intent;
import android.os.IBinder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import roboguice.service.RoboService;

import java.util.concurrent.atomic.AtomicInteger;

public class IOIOService extends RoboService implements IOIOLooperProvider
{
  @Inject SensorManager sensorManager;
  @Inject SoundHelper soundHelper;
  @Inject EventBus eventBus;

  AtomicInteger redPower = new AtomicInteger();
  AtomicInteger greenPower = new AtomicInteger();
  AtomicInteger bluePower = new AtomicInteger();

  private final IOIOAndroidApplicationHelper helper = new IOIOAndroidApplicationHelper(this, this);
  private boolean started = false;

  @Override
  public void onCreate()
  {
    super.onCreate();
    helper.create();
  }

  @Override
  public void onStart(Intent intent, int startId)
  {
    super.onStart(intent, startId);
    if (started)
    {
      helper.restart();
    }
    else
    {
      helper.start();
      started = true;
    }
    eventBus.register(this);
  }

  @Override
  public void onDestroy()
  {
    stop();
    helper.destroy();
    super.onDestroy();
  }

  protected void stop()
  {
    if (started)
    {
      helper.stop();
      started = false;
      eventBus.unregister(this);
    }
  }

  @Subscribe
  public void onEvent(SensorEvent event)
  {
    Sensor visibleSensor = sensorManager.getVisibleSensor();
    if(visibleSensor.matches(event))
    {
      MeasurementLevel level = soundHelper.level(visibleSensor, event.getValue());
      switch (level)
      {
        case TOO_LOW:
          setColors(Colors.BLACK);
          break;
        case VERY_LOW:
          setColors(Colors.GREEN);
          break;
        case LOW:
          setColors(Colors.GREEN);
          break;
        case MID:
          setColors(Colors.YELLOW);
          break;
        case HIGH:
          setColors(Colors.ORANGE);
          break;
        case VERY_HIGH:
          setColors(Colors.RED);
          break;
      }
    }
  }

  private void setColors(Colors color)
  {
    redPower.set(color.getRed());
    greenPower.set(color.getGreen());
    bluePower.set(color.getBlue());
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }

  @Override
  public IOIOLooper createIOIOLooper(String connectionType, Object extra)
  {
    return new BaseIOIOLooper()
    {
      private final int BLUE_LED_PIN = 34;
      private final int RED_LED_PIN = 35;
      private final int GREEN_LED_PIN = 36;

      private PwmOutput blueLed;
      private PwmOutput redLed;
      private PwmOutput greenLed;

      @Override
      protected void setup() throws ConnectionLostException,
                                    InterruptedException
      {
        blueLed = ioio_.openPwmOutput(BLUE_LED_PIN, 1000);
        redLed = ioio_.openPwmOutput(RED_LED_PIN, 1000);
        greenLed = ioio_.openPwmOutput(GREEN_LED_PIN, 1000);
      }

      @Override
      public void loop() throws ConnectionLostException,
                                InterruptedException
      {
        redLed.setPulseWidth(redPower.get());
        greenLed.setPulseWidth(greenPower.get());
        blueLed.setPulseWidth(bluePower.get());
        Thread.sleep(100);
      }
    };
  }
}

