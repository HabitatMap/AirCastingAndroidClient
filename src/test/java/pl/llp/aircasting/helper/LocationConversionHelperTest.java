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
package pl.llp.aircasting.helper;

import com.google.android.maps.GeoPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.stream.map.LocationConversionHelper;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/22/11
 * Time: 10:20 AM
 */
@RunWith(InjectedTestRunner.class)
public class LocationConversionHelperTest {
    Session session;

    @Before
    public void setup() {
        session = new Session();

        MeasurementStream stream1 = mock(MeasurementStream.class);
        when(stream1.getSensorName()).thenReturn("LHC");
        when(stream1.getMeasurements())
                .thenReturn(newArrayList(new Measurement(10, 20, 10), new Measurement(20, 10, 10)));

        MeasurementStream stream2 = mock(MeasurementStream.class);
        when(stream2.getSensorName()).thenReturn("LHC2");
        when(stream2.getMeasurements()).thenReturn(newArrayList(new Measurement(-10, 30, 10)));

        session.add(stream1);
        session.add(stream2);
    }

    @Test
    public void shouldCalculateBoundingBox() {
        LocationConversionHelper.BoundingBox boundingBox = LocationConversionHelper.boundingBox(session);

        assertThat(boundingBox.getCenter(), equalTo(new GeoPoint(5 * 1000000, 20 * 1000000)));
        assertThat(boundingBox.getLatSpan(), equalTo(30 * 1000000));
        assertThat(boundingBox.getLonSpan(), equalTo(20 * 1000000));
    }
}
