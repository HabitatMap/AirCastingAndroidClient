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

import android.location.LocationListener;
import android.location.LocationManager;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import pl.llp.aircasting.InjectedTestRunner;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/13/11
 * Time: 1:57 PM
 */
@RunWith(InjectedTestRunner.class)
public class LocationHelperTest {
    @Inject LocationHelper locationHelper;

    @Before
    public void setup(){
        locationHelper.locationManager = mock(LocationManager.class);
    }

    @Test
    public void shouldNotBeTrickedIntoThinkingItNedNotStop(){
        locationHelper.stop();
        reset(locationHelper.locationManager);

        locationHelper.start();
        locationHelper.stop();
        
        verify(locationHelper.locationManager, times(1)).removeUpdates(Mockito.any(LocationListener.class));
    }
}
