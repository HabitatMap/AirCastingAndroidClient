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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Session;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/19/11
 * Time: 3:18 PM
 */
public class EditSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.save_button) Button saveButton;
    @InjectView(R.id.discard_button) Button discardButton;

    @InjectView(R.id.session_title) EditText sessionTitle;
    @InjectView(R.id.session_tags) EditText sessionTags;
    @InjectView(R.id.session_description) EditText sessionDescription;

    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_details);

        session = (Session) getIntent().getSerializableExtra(Intents.SESSION);
        sessionTitle.setText(session.getTitle());
        sessionTags.setText(session.getTags());
        sessionDescription.setText(session.getDescription());

        saveButton.setOnClickListener(this);

        discardButton.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        session.setTitle(sessionTitle.getText().toString());
        session.setDescription(sessionDescription.getText().toString());
        session.setTags(sessionTags.getText().toString());

        Intent intent = new Intent();
        intent.putExtra(Intents.SESSION, session);
        setResult(view.getId(), intent);

        finish();
    }
}
