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

import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import pl.llp.aircasting.R;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/22/11
 * Time: 2:56 PM
 */
public class DialogActivity extends RoboActivityWithProgress {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().setLayout(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_dialog_menu_generic);
    }
}
