package pl.llp.aircasting.helper;

import pl.llp.aircasting.model.SessionManager;

import android.view.View;
import com.google.inject.Inject;

/**
 * Created by ags on 02/04/2013 at 20:52
 */
public class NowValueVisibilityManager
{
  @Inject SessionManager sessionManager;

  int getVisibility()
  {
    boolean shouldDisplay = sessionManager.isSessionRecording() || !sessionManager.isSessionBeingViewed();
    int visibility = shouldDisplay ? View.VISIBLE : View.GONE;
    return visibility;
  }
}
