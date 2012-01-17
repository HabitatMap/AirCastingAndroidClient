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
package pl.llp.aircasting.model;

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 9/28/11
 * Time: 1:06 PM
 */
@RunWith(InjectedTestRunner.class)
public class SoundMeasurementTest {
    @Inject Gson gson;

    SoundMeasurement measurement;
    SoundMeasurement other;

    @Before
    public void setup() {
        measurement = new SoundMeasurement(0, 1, 2);
        other = new SoundMeasurement(0, 1, 2);
    }

    @Test
    public void shouldEqualWhenAllFieldsAreSame() {
        assertThat(measurement, equalTo(other));
        assertThat(measurement.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void shouldNotEqualWhenLatitudeDiffers() {
        other.setLatitude(10);
        assertThat(measurement, not(equalTo(other)));
        assertThat(measurement.hashCode(), not(equalTo(other.hashCode())));
    }

    @Test
    public void shouldTimestampItself() {
        assertThat(Calendar.getInstance().getTimeInMillis() - measurement.getTime().getTime() < 1000, equalTo(true));
    }

    @Test
    public void shouldExposeMembersToGSON(){
        // JSON loses sub-second parts, but we don't care
        measurement.setTime(new Date(0));
        SoundMeasurement result = gson.fromJson(gson.toJson(measurement), SoundMeasurement.class);

        assertThat(result.getTime(), equalTo(measurement.getTime()));
        assertThat(result.getLatitude(), equalTo(measurement.getLatitude()));
        assertThat(result.getLongitude(), equalTo(measurement.getLongitude()));
        assertThat(result.getValue(), equalTo(measurement.getValue()));
    }
}
