package pl.llp.aircasting.helper;

import android.view.View;
import com.google.inject.Inject;

/**
 * Created by ags on 02/04/2013 at 20:52
 */
public class NowValueVisibilityManager {
    @Inject VisibleSession visibleSession;

    int getVisibility() {
        boolean shouldDisplay = visibleSession.isCurrentSessionVisible();
        int visibility = shouldDisplay ? View.VISIBLE : View.GONE;
        return visibility;
    }
}
