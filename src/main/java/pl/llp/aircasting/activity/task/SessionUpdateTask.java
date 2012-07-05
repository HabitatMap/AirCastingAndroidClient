package pl.llp.aircasting.activity.task;

import pl.llp.aircasting.activity.ActivityWithProgress;
import pl.llp.aircasting.repository.ProgressListener;
import pl.llp.aircasting.service.SessionSyncer;

import android.app.ProgressDialog;

/**
 * Created by ags on 05/07/12 at 22:19
 */
public class SessionUpdateTask extends SimpleProgressTask<Void, Void, Void> implements ProgressListener
{
  private SessionSyncer syncer;

  public SessionUpdateTask(ActivityWithProgress context, SessionSyncer syncer)
  {
    super(context, ProgressDialog.STYLE_HORIZONTAL);
    this.syncer = syncer;
  }

  @Override
  protected Void doInBackground(Void... voids)
  {
    syncer.performSync(this);
    return null;
  }

  @Override
  public void onSizeCalculated(int workSize)
  {
    dialog.setMax(workSize);
  }

  @Override
  public void onProgress(int progress)
  {
    dialog.setProgress(progress);
  }
}