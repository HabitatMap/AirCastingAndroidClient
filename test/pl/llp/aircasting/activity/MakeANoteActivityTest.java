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
package pl.llp.aircasting.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.GeocodingHelper;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.model.SessionManager;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.*;
import static pl.llp.aircasting.TestHelper.click;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/20/11
 * Time: 1:08 PM
 */
@RunWith(InjectedTestRunner.class)
public class MakeANoteActivityTest {
    @Inject MakeANoteActivity activity;
    private Location location;

    @Before
    public void setup() throws IOException {
        activity.onCreate(null);

        activity.sessionManager = mock(SessionManager.class);
        activity.geocodingHelper = mock(GeocodingHelper.class);
        activity.locationHelper = mock(LocationHelper.class);

        location = new Location("TEST");

        when(activity.geocodingHelper.getFromLocation(Mockito.any(Location.class))).thenReturn("Test st.");
        when(activity.locationHelper.getLastLocation()).thenReturn(location);
        when(activity.sessionManager.getAvg(10)).thenReturn(3.0);
        when(activity.sessionManager.getPeak(10)).thenReturn(4.0);

        activity.onResume();
    }

    @Test
    public void shouldSaveANote() {
        activity.noteText.setText("Note text");

        click(activity, R.id.save_button);

        verify(activity.sessionManager).makeANote(activity.date, "Note text", null);
    }

    @Test
    public void shouldFillNoteText() {
        assertThat(activity.noteText.getText().toString(), containsString("4 dB peak"));
        assertThat(activity.noteText.getText().toString(), containsString("3 dB average"));
        assertThat(activity.noteText.getText().toString(), containsString("near Test st."));
    }

    @Test
    public void shouldPersistPhotoOnRotation() {
        activity.photoPath = "some/path";

        Bundle bundle = mock(Bundle.class);
        activity.onSaveInstanceState(bundle);

        verify(bundle).putString(MakeANoteActivity.PHOTO_PATH, "some/path");
    }

    @Test
    public void shouldHideAttachPhotoButton() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.containsKey(MakeANoteActivity.PHOTO_ATTACHED)).thenReturn(true);
        when(bundle.getBoolean(MakeANoteActivity.PHOTO_ATTACHED)).thenReturn(true);

        activity.onCreate(bundle);
        activity.onRestoreInstanceState(bundle);
        activity.onResume();

        assertThat(activity.attachPhoto.getVisibility(), equalTo(View.GONE));
    }
}
