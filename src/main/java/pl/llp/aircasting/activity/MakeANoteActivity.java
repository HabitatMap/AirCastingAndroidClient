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

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.helper.CalibrationHelper;
import pl.llp.aircasting.helper.FormatHelper;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.PhotoHelper;
import pl.llp.aircasting.model.SessionManager;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.io.IOException;
import java.util.Date;

public class MakeANoteActivity extends DialogActivity implements View.OnClickListener
{
    public static final String PHOTO_PATH = "picture_path";
    public static final String PHOTO_ATTACHED = "photo_attached";

    @Inject SessionManager sessionManager;
    @Inject LocationHelper locationHelper;
    @Inject PhotoHelper photoHelper;
    @Inject Application context;

    @Inject CalibrationHelper calibrationHelper;

    @InjectView(R.id.attach_photo) Button attachPhoto;
    @InjectView(R.id.note_text) EditText noteText;
    @InjectView(R.id.save_button) Button save;
    @InjectView(R.id.date) TextView dateText;
    @InjectView(R.id.share) Button share;

    @InjectResource(R.string.im_aircasting) String imAircasting;
    @InjectResource(R.string.share_with) String shareWith;

    boolean photoAttached;
    String photoPath;
    Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.make_a_note);

        date = new Date();
        dateText.setText(FormatHelper.dateTime(date));

        save.setOnClickListener(this);
        share.setOnClickListener(this);
        attachPhoto.setOnClickListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PHOTO_PATH)) {
                photoPath = savedInstanceState.getString(PHOTO_PATH);
            }
            if (savedInstanceState.containsKey(PHOTO_ATTACHED)) {
                photoAttached = savedInstanceState.getBoolean(PHOTO_ATTACHED);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (photoAttached) {
            attachPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PHOTO_PATH, photoPath);
        outState.putBoolean(PHOTO_ATTACHED, photoAttached);
    }

    @Override
    public void onClick(View view) {
      String text = noteText.getText().toString();
      if (Strings.isNullOrEmpty(text))
      {
        Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
        return;
      }
      switch (view.getId()) {
            case R.id.share:
                Intents.share(this, shareWith, imAircasting, text);
            case R.id.save_button:
                sessionManager.makeANote(date, text, photoPath);
                finish();
                break;
            case R.id.attach_photo:
                takePhoto();
                break;
        }
    }

    private void takePhoto() {
        try {
            photoPath = Intents.takePhoto(this);
        } catch (IOException e) {
            Toast.makeText(context, R.string.storage_error, Toast.LENGTH_LONG).show();
            Logger.e("Error while attaching a photo", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Intents.TAKE_PICTURE:
                processBitmap();
                break;
        }
    }

    private void processBitmap() {
        if (photoPath != null && photoHelper.photoExists(photoPath)) {
            Toast.makeText(context, R.string.photo_success, Toast.LENGTH_LONG).show();

            attachPhoto.setVisibility(View.GONE);
            photoAttached = true;
        }
    }
}
