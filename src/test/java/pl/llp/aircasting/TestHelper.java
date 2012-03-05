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
package pl.llp.aircasting;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/9/11
 * Time: 11:34 AM
 */
public class TestHelper {
    public static void click(Activity activity, int id) {
        View view = activity.findViewById(id);
        view.performClick();
    }

    public static void check(Activity activity, int id) {
        CheckBox checkBox = (CheckBox) activity.findViewById(id);
        checkBox.setChecked(true);
    }

    public static void unCheck(Activity activity, int id) {
        CheckBox checkBox = (CheckBox) activity.findViewById(id);
        checkBox.setChecked(false);
    }

    public static void fill(Activity activity, int id, String text) {
        EditText editText = (EditText) activity.findViewById(id);
        shadowOf(activity).setCurrentFocus(editText);
        editText.setText(text);
    }
}
