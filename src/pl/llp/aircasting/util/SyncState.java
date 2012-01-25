package pl.llp.aircasting.util;

import com.google.inject.Singleton;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/25/12
 * Time: 1:15 PM
 */
@Singleton
public class SyncState {
    private boolean inProgress = false;

    public synchronized boolean isInProgress() {
        return inProgress;
    }

    public synchronized void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
