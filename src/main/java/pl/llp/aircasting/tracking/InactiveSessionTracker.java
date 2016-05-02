package pl.llp.aircasting.tracking;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.SessionPropertySetter;
import pl.llp.aircasting.storage.db.DBConstants;
import pl.llp.aircasting.util.Constants;

import android.util.Log;
import com.google.common.base.Preconditions;

import static pl.llp.aircasting.storage.db.DBConstants.SESSION_TAGS;

/**
 * Created by ags on 03/16/13 at 23:36
 */
public class InactiveSessionTracker implements SessionTracker
{
  private final SessionPropertySetter setter;

  public InactiveSessionTracker(DatabaseTaskQueue dbQueue)
  {
    Preconditions.checkNotNull(dbQueue, "Taks queue can't be null!");
    this.setter = new SessionPropertySetter(dbQueue);
  }

  @Override
  public void addNote(Note note)
  {
    doNothingAndComplain();
  }

  private void doNothingAndComplain()
  {
    Log.e(Constants.TAG, "Should not have been called!", new Throwable());
    throw new RuntimeException("Shouldn't happen");
  }

  @Override
  public void finishTracking()
  {
    // do nothing
  }

  @Override
  public void setTitle(long sessionId, String title)
  {
    setter.forSession(sessionId).key(DBConstants.SESSION_TITLE).value(title).doSet();
  }

  @Override
  public void setTags(long sessionId, String tags)
  {
    setter.forSession(sessionId).key(SESSION_TAGS).value(tags).doSet();
  }

  @Override
  public void setDescription(long sessionId, String description)
  {
    setter.forSession(sessionId).key(DBConstants.SESSION_DESCRIPTION).value(description).doSet();
  }

  @Override
  public void setContribute(long sessionId, boolean shouldContribute)
  {
    setter.forSession(sessionId).key(DBConstants.SESSION_CONTRIBUTE).value(shouldContribute).doSet();
  }

  @Override
  public void addStream(MeasurementStream stream)
  {
    doNothingAndComplain();
  }

  @Override
  public void addMeasurement(MeasurementStream stream, Measurement measurement)
  {
    doNothingAndComplain();
  }

  @Override
  public void delete(long sessionId)
  {
    // do nothing
  }

  @Override
  public boolean save(Session session)
  {
    doNothingAndComplain();
    return false;
  }
}
