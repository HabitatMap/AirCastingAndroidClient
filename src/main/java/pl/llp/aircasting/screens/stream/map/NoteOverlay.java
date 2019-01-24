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
package pl.llp.aircasting.screens.stream.map;

import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import pl.llp.aircasting.screens.common.helpers.FormatHelper;
import pl.llp.aircasting.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/20/11
 * Time: 2:14 PM
 */
public class NoteOverlay extends ItemizedOverlay {
    AirCastingMapActivity context;

    List<OverlayItem> items = new ArrayList<OverlayItem>();

    public NoteOverlay(Drawable drawable) {
        super(boundCenterBottom(drawable));
    }

    public void setContext(AirCastingMapActivity context) {
        this.context = context;
    }

    @Override
    protected OverlayItem createItem(int i) {
        return items.get(i);
    }

    @Override
    public int size() {
        return items.size();
    }

    public void add(Note note) {
        GeoPoint geoPoint = LocationConversionHelper.geoPoint(note.getLatitude(), note.getLongitude());
        String title = FormatHelper.dateTime(note.getDate()).toString();
        OverlayItem item = new OverlayItem(geoPoint, title, note.getText());

        items.add(item);

        populate();
    }

    @Override
    public boolean onTap(int i) {
        context.noteClicked(items.get(i), i);
        return true;
    }

    public void clear() {
        items.clear();
        populate();
    }
}
