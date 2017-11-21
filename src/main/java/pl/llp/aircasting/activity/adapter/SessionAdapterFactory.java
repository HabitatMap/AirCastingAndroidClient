package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.helper.ResourceHelper;
import android.content.Context;
import com.google.inject.Inject;
import pl.llp.aircasting.helper.SessionState;

/**
 * Created by ags on 05/07/12 at 21:06
 */
public class SessionAdapterFactory {
  @Inject ResourceHelper resourceHelper;
  @Inject SessionState sessionState;

  public SessionAdapter getSessionAdapter(Context context) {
    SessionAdapter adapter = new SessionAdapter(context, sessionState);

    adapter.setResourceHelper(resourceHelper);

    return adapter;
  }
}
