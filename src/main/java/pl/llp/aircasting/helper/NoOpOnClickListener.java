package pl.llp.aircasting.helper;

import android.content.DialogInterface;

/**
 * Created by ags on 19/07/12 at 14:55
 */
public class NoOpOnClickListener implements DialogInterface.OnClickListener
{
  @Override
  public void onClick(DialogInterface dialog, int which)
  {
    // be like me - do nothing!
  }
}
