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
package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.repository.SessionRepository;

import android.content.Context;
import android.database.Cursor;
import com.google.inject.Inject;

public class SessionCursorAdapterFactory
{
  @Inject SessionRepository sessionRepository;
  @Inject ResourceHelper resourceHelper;

  public SessionCursorAdapter getSessionAdapter(Context context, Cursor cursor)
  {
    SessionCursorAdapter adapter = new SessionCursorAdapter(context, cursor);
    adapter.setSessionRepository(sessionRepository);
    adapter.setResourceHelper(resourceHelper);

    return adapter;
  }
}
