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

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.CalibrationHelper;
import pl.llp.aircasting.helper.FormatHelper;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.SessionRepository;

import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/5/11
 * Time: 4:27 PM
 */
public class SessionAdapter extends SimpleCursorAdapter {
    SessionRepository sessionRepository;
    ResourceHelper resourceHelper;

    private static final int[] backgrounds = new int[]{R.drawable.session_list_odd, R.drawable.session_list_even};
    private Context context;
    private CalibrationHelper calibrationHelper;

    public SessionAdapter(Context context, Cursor cursor) {
        super(context, R.layout.session_row, cursor, new String[]{}, new int[]{});
        this.context = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Session session = sessionRepository.load(cursor);

        TextView sessionTitle = (TextView) view.findViewById(R.id.session_title);
        if (session.getTitle() != null && session.getTitle().length() > 0) {
            sessionTitle.setText(session.getTitle());
        } else {
            String unnamed = context.getString(R.string.unnamed_session);
            sessionTitle.setText(Html.fromHtml(unnamed));
        }

        int peak = (int) calibrationHelper.calibrate(session.getPeak(), session);
        int avg = (int) calibrationHelper.calibrate(session.getAvg(), session);

        ((TextView) view.findViewById(R.id.session_peak)).setText(NumberFormat.getInstance().format(peak) + " dB");
        ((TextView) view.findViewById(R.id.session_average)).setText(NumberFormat.getInstance().format(avg) + " dB");

        updateImage((ImageView) view.findViewById(R.id.session_average_marker), avg);
        updateImage((ImageView) view.findViewById(R.id.session_peak_marker), peak);

        ((TextView) view.findViewById(R.id.session_time)).setText(FormatHelper.timeText(session));
    }

    private void updateImage(ImageView view, double value) {
        Drawable bullet = resourceHelper.getBulletAbsolute(value);
        view.setBackgroundDrawable(bullet);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Drawable background = evenOddBackground(position);
        view.setBackgroundDrawable(background);

        return view;
    }

    private Drawable evenOddBackground(int position) {
        int id = backgrounds[position % backgrounds.length];
        return context.getResources().getDrawable(id);
    }

    public void setSessionRepository(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void setResourceHelper(ResourceHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
    }

    public void setCalibrationHelper(CalibrationHelper calibrationHelper) {
        this.calibrationHelper = calibrationHelper;
    }
}
