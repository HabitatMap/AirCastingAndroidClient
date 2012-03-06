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
package pl.llp.aircasting.activity.task;

import android.app.ProgressDialog;
import pl.llp.aircasting.activity.ActivityWithProgress;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.SessionRepository;

public abstract class OpenSessionTask extends SimpleProgressTask<Long, Void, Session> implements SessionRepository.ProgressListener {
    public OpenSessionTask(ActivityWithProgress context) {
        super(context, ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    protected void onPostExecute(Session session) {
        dialog.dismiss();
    }

    @Override
    public void onSizeCalculated(int workSize) {
        dialog.setMax(workSize);
    }

    @Override
    public void onProgress(int progress) {
        dialog.setProgress(progress);
    }
}
