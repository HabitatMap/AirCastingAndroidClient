package pl.llp.aircasting.screens.common.helpers;

import pl.llp.aircasting.storage.ProgressListener;

import android.content.DialogInterface;

/**
 * Created by ags on 19/07/12 at 14:55
 */
public class NoOp
{
  public static DialogInterface.OnClickListener dialogOnClick()
  {
    return new  DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
      }
    };
  }

  public static ProgressListener progressListener()
  {
    return new ProgressListener()
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
    };
  }
}
