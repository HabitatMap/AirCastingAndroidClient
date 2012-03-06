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

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/14/11
 * Time: 12:57 PM
 */
@RunWith(InjectedTestRunner.class)
public class GeocodingHelperTest {
    @Inject GeocodingHelper geocodingHelper;

    private Location location;
    private List<Address> addresses = new ArrayList<Address>();

    @Before
    public void setup() throws IOException {
        Address address = mock(Address.class);
        when(address.getThoroughfare()).thenReturn("Something");
        addresses.add(address);

        location = new Location("TEST");
        location.setLongitude(123);
        location.setLatitude(423);

        geocodingHelper.geocoder = mock(Geocoder.class);
        geocodingHelper.locationHelper = mock(LocationHelper.class);

        when(geocodingHelper.locationHelper.getLastLocation()).thenReturn(location);

        when(geocodingHelper.geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1)).
                thenReturn(addresses);
    }

    @Test
    public void shouldHandleNoMatches() throws IOException {
        when(geocodingHelper.geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1)).
                thenReturn(new ArrayList<Address>());

        assertThat(geocodingHelper.getFromLocation(location), equalTo(null));
    }

    @Test
    public void shouldHandleMatches() throws IOException {
        assertThat(geocodingHelper.getFromLocation(location), equalTo("Something"));
    }
}
