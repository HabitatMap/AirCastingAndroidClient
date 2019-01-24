package pl.llp.aircasting.screens.stream;

import android.view.View;
import com.google.inject.Inject;

import pl.llp.aircasting.screens.common.sessionState.VisibleSession;

/**
 * Created by ags on 02/04/2013 at 20:52
 */
public class NowValueVisibilityManager {
    @Inject
    VisibleSession visibleSession;

    int getVisibility() {
        boolean shouldDisplay = visibleSession.isVisibleSessionFixed() || visibleSession.isCurrentSessionVisible();
        int visibility = shouldDisplay ? View.VISIBLE : View.GONE;
        return visibility;
    }
}
