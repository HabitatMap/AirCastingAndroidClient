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

import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.repository.ProgressListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

public class ContributeActivity extends DialogActivity implements View.OnClickListener
{
  @Inject SessionManager sessionManager;
  @Inject Context context;
  @Inject SettingsHelper settingsHelper;

  @InjectView(R.id.yes) Button yes;
  @InjectView(R.id.no) Button no;

  @Override
  protected void onResume()
  {
    super.onResume();
    if (!settingsHelper.hasCredentials())
    {
      Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.contribute);

    yes.setOnClickListener(this);
    no.setOnClickListener(this);
  }

  @Override
  public void onClick(View view)
  {
    boolean contribute = view.getId() == R.id.yes;
    sessionManager.setContribute(contribute);

    saveSession();
  }

  @Override
  public void onBackPressed()
  {
    sessionManager.continueSession();
    finish();
  }

  private void saveSession()
  {
    //noinspection unchecked
    new SaveSessionTask(this).execute();
  }

  private class SaveSessionTask extends SimpleProgressTask<Void, Void, Void> implements ProgressListener
  {
    public SaveSessionTask(ActivityWithProgress context)
    {
      super(context, ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
      sessionManager.finishSession(this);

      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
      dialog.dismiss();
      finish();
    }

    @Override
    public void onSizeCalculated(final int workSize)
    {

    }

    @Override
    public void onProgress(final int progress)
    {

    }
  }
}
