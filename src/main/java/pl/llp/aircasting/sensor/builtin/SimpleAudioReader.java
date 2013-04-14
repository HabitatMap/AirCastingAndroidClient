/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.sensor.builtin;

import pl.llp.aircasting.event.sensor.AudioReaderErrorEvent;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.helper.CalibrationHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Sensor;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class SimpleAudioReader extends AudioReader.Listener
{
  private static final int SAMPLE_RATE = 44100;
  public static final String SYMBOL = "dB";
  public static final String UNIT = "decibels";
  public static final String MEASUREMENT_TYPE = "Sound Level";
  public static final String SHORT_TYPE = "dB";
  public static final String SENSOR_NAME = "Phone Microphone";
  public static final String SENSOR_PACKAGE_NAME = "Builtin";
  public static final String SENSOR_ADDRESS_BUILTIN = "none";

  public static final int VERY_LOW = 20;
  public static final int LOW = 60;
  public static final int MID = 70;
  public static final int HIGH = 80;
  public static final int VERY_HIGH = 100;

  public static final Sensor sensor = new Sensor(
      SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT, SYMBOL, VERY_LOW, LOW, MID, HIGH, VERY_HIGH, SENSOR_ADDRESS_BUILTIN
  );

  @Inject SettingsHelper settingsHelper;
  @Inject AudioReader audioReader;
  @Inject SignalPower signalPower;
  @Inject EventBus eventBus;
  @Inject CalibrationHelper calibrationHelper;

  /**
   * @return A Sensor representing the internal microphone
   */
  public static Sensor getSensor() {
    return sensor;
  }

  public void start() {
    // The AudioReader sleeps as much as it records
    int block = SAMPLE_RATE / 2;

    audioReader.startReader(SAMPLE_RATE, block, this);
  }

  public void stop() {
    audioReader.stopReader();
  }

  @Override
  public void onReadComplete(short[] buffer) {
    Double power = signalPower.calculatePowerDb(buffer);
    if (power != null) {
      double calibrated = calibrationHelper.calibrate(power);
      SensorEvent event = new SensorEvent(SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT, SYMBOL,
                                          VERY_LOW, LOW, MID, HIGH, VERY_HIGH, calibrated);
      eventBus.post(event);
    }
  }

  @Override
  public void onReadError(int error) {
    eventBus.post(new AudioReaderErrorEvent());
  }
}
