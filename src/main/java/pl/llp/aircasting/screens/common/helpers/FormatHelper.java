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
package pl.llp.aircasting.screens.common.helpers;

import com.google.inject.Inject;

import pl.llp.aircasting.model.Session;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatHelper {
    @Inject SettingsHelper mSettingsHelper;

    // 01/29/12 10:22
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    // 10:22
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    // 6:20 pm
//    public static final SimpleDateFormat m12HourFormat = new SimpleDateFormat("HH:mma");
    public static final SimpleDateFormat m12HourFormat = get12HourFormat();

    private static SimpleDateFormat get12HourFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mma");
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        symbols.setAmPmStrings(new String[] { "am", "pm" });
        sdf.setDateFormatSymbols(symbols);
        return sdf;
    }

    public String sessionDatetime(Session session) {
        if (session.isFixed()) {
            return dateFormat.format(session.getStart()) + " " + getTime(session.getStart());
        } else {
            return dateFormat.format(session.getStart()) + " " + getTime(session.getStart()) + " - " + getTime(session.getEnd());
        }
    }

    public CharSequence noteDatetime(Date date) {
        return dateFormat.format(date) + " " + getTime(date);
    }

    public String getTimestamp(Date date) {
        return getTime(date) + " " + dateFormat.format(date);
    }

    public String getTime(Date date) {
        if (mSettingsHelper.defaultTimeFormat()) {
            return timeFormat.format(date);
        } else {
            return m12HourFormat.format(date);
        }
    }
}