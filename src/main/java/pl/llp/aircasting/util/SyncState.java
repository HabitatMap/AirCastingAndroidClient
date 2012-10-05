package pl.llp.aircasting.util;

import com.google.inject.Singleton;

@Singleton
public class SyncState {

  public enum States
  {
    IN_PROGRESS,
    NOT_IN_PROGRESS
  }

  private States state = States.NOT_IN_PROGRESS;

  public synchronized boolean isInProgress()
  {
    return state == States.IN_PROGRESS;
  }

  public synchronized void setInProgress(boolean inProgress)
  {
    state = inProgress ? States.IN_PROGRESS : States.NOT_IN_PROGRESS;
  }
}
