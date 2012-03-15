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

import com.google.inject.Inject;
import pl.llp.aircasting.helper.SettingsHelper;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/21/11
 * Time: 2:20 PM
 */
public class SimpleAudioReader extends AudioReader.Listener {
    private static final int SAMPLE_RATE = 44100;

    @Inject SettingsHelper settingsHelper;
    @Inject AudioReader audioReader;
    @Inject SignalPower signalPower;

    private SoundVolumeListener listener;

    public void start(SoundVolumeListener newListener) {
        this.listener = newListener;

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
            listener.onMeasurement(power);
        }
    }

    @Override
    public void onReadError(int error) {
        listener.onError();
    }
}
