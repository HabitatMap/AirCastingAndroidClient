package pl.llp.aircasting.repository;

/**
 * Created by ags on 30/03/12 at 11:05
 */
public class NullProgressListener implements ProgressListener
{
  @Override
  public void onSizeCalculated(int workSize)
  {
    // do nothing
  }

  @Override
  public void onProgress(int progress)
  {
    // do nothing
  }
}
