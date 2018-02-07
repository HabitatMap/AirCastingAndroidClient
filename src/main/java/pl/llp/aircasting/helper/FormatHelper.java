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

import pl.llp.aircasting.model.Session;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatHelper {
    // 01/29/12 10:22
    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yy HH:mm");

    // 10:22
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public static String timeText(Session session) {
        if (session.isFixed()) {
            return getStartTime(session);
        } else {
            return getStartTime(session) + " - " + timeFormat.format(session.getEnd());

        }
    }

    public static CharSequence dateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    private static String getStartTime(Session session) {
        return dateTimeFormat.format(session.getStart());
    }
}