package pl.llp.aircasting.activity.task;

import pl.llp.aircasting.activity.ActivityWithProgress;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.repository.ProgressListener;

import android.app.ProgressDialog;

/**
 * Created by ags on 03/03/13 at 18:00
 */
public class SaveSessionTask extends SimpleProgressTask<Void, Void, Void> implements ProgressListener
{
  SessionManager sessionManager;

  public SaveSessionTask(ActivityWithProgress context, SessionManager sessionManager)
  {
    super(context, ProgressDialog.STYLE_SPINNER);
    this.sessionManager = sessionManager;
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