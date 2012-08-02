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
package pl.llp.aircasting.activity.menu;

import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.AboutActivity;
import pl.llp.aircasting.activity.SessionsActivity;
import pl.llp.aircasting.activity.SettingsActivity;
import pl.llp.aircasting.activity.SoundTraceActivity;
import pl.llp.aircasting.model.SessionManager;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.inject.Inject;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/30/11
 * Time: 12:59 PM
 */
public class MainMenu {
    @Inject SessionManager sessionManager;
    @Inject Application context;

    public boolean create(Activity activity, Menu menu) {
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.aircasting, menu);

        return true;
    }

    public boolean handleClick(Activity activity, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aircasting:
                if (sessionManager.isSessionSaved()) {
                    sessionManager.discardSession();
                }
              Intent intent = new Intent(context, SoundTraceActivity.class);
              intent.putExtra("reconsiderCurrentSensor", true);
              activity.startActivity(intent);
                break;
            case R.id.sessions:
                activity.startActivity(new Intent(context, SessionsActivity.class));
                break;
            case R.id.settings:
                activity.startActivity(new Intent(context, SettingsActivity.class));
                break;
            case R.id.about:
                activity.startActivity(new Intent(context, AboutActivity.class));
                break;
        }
        return true;
    }
}
