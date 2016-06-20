package pl.llp.aircasting.tracking;

import pl.llp.aircasting.model.*;

/**
 * Created by ags on 03/14/13 at 23:37
 */
public interface SessionTracker
{
  void addNote(Note note);

  void finishTracking();

  void setTitle(long sessionId, String title);

  void setTags(long sessionId, String tags);

  void setDescription(long sessionId, String description);

  void setContribute(long sessionId, boolean shouldContribute);

  void addStream(MeasurementStream stream);

  void addMeasurement(Sensor sensor, MeasurementStream stream, Measurement measurement);

  void delete(long sessionId);

  boolean save(Session session);

  double getNow(Sensor sensor);
}
