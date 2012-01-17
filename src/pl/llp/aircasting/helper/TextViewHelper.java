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

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/16/11
 * Time: 3:13 PM
 */
public class TextViewHelper {
    public static Spanned stripUnderlines(Spanned spanned) {
        SpannableString spannable = new SpannableString(spanned);

        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);

        for (URLSpan span : spans) {
            URLSpan noUnderline = new URLSpanNoUnderline(span.getURL());

            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);

            spannable.removeSpan(span);
            spannable.setSpan(noUnderline, start, end, 0);
        }

        return spannable;
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
