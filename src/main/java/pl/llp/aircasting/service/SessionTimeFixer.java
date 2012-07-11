package pl.llp.aircasting.service;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;

import java.util.Date;
import java.util.List;

public class SessionTimeFixer
{
  public void fromUTCtoLocal(Session session)
  {
    int minutes = getOffset(session);
    int offset = minutes * 60 * 1000;

    session.setStart(new Date(session.getStart().getTime() + offset));
    session.setEnd(new Date(session.getEnd().getTime() + offset));

    List<MeasurementStream> streams = session.getActiveMeasurementStreams();
    for (MeasurementStream stream : streams)
    {
      List<Measurement> measurements = stream.getMeasurements();
      for (Measurement measurement : measurements)
      {
        measurement.setTime(new Date(measurement.getTime().getTime() + offset));
      }
    }
  }

  private int getOffset(Session session)
  {
    List<MeasurementStream> streams = session.getActiveMeasurementStreams();
    for (MeasurementStream stream : streams)
    {
      List<Measurement> measurements = stream.getMeasurements();
      for (Measurement measurement : measurements)
      {
        return measurement.getTimeZoneOffsetMinutes();
      }
    }
    return 0;
  }
}
