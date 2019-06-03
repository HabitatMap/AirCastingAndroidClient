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
package pl.llp.aircasting.screens.sessions.shareSession;

import android.app.Activity;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Session;
import roboguice.inject.InjectResource;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/5/11
 * Time: 12:08 PM
 */
public class ShareHelper {
    @InjectResource(R.string.session_link_template) String sessionLinkTemplate;
    @InjectResource(R.string.share_link) String shareLink;
    @InjectResource(R.string.share_title) String shareTitle;

    public void shareLink(Activity activity, Session session, CharSequence selectedSensor) {
        String text = String.format(sessionLinkTemplate, session.getLocation());

        Intents.share(activity, shareLink, shareTitle, text);
    }
}
