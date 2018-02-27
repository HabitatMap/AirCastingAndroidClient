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
import android.view.View;
import android.widget.Button;
import pl.llp.aircasting.R;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/11/11
 * Time: 4:25 PM
 */
public class OpenFixedSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.view) Button view;
    @InjectView(R.id.continue_streaming) Button continueStreaming;
    @InjectView(R.id.share) Button share;
    @InjectView(R.id.edit) Button edit;
    @InjectView(R.id.delete_session) Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_fixed_session);
        initDialogToolbar("Session Options");

        view.setOnClickListener(this);
        continueStreaming.setOnClickListener(this);
        share.setOnClickListener(this);
        edit.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        setResult(view.getId());
        finish();
    }
}
