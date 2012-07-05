package pl.llp.aircasting.activity.task;

import pl.llp.aircasting.activity.ActivityWithProgress;
import pl.llp.aircasting.repository.db.UncalibratedMeasurementCalibrator;
import pl.llp.aircasting.repository.ProgressListener;
import pl.llp.aircasting.repository.db.UncalibratedMeasurementCalibrator;

import android.app.ProgressDialog;

public class CalibrateSessionsTask extends SimpleProgressTask<Void, Void, Void> implements ProgressListener
{
  private UncalibratedMeasurementCalibrator calibrator;

  public CalibrateSessionsTask(ActivityWithProgress context, UncalibratedMeasurementCalibrator calibrator)
  {
    super(context, ProgressDialog.STYLE_HORIZONTAL);
    this.calibrator = calibrator;
  }

  @Override
  protected Void doInBackground(Void... voids)
  {
    int count = calibrator.sessionsToCalibrate();
    if(count > 0)
    {
      calibrator.calibrate(this);
    }
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
