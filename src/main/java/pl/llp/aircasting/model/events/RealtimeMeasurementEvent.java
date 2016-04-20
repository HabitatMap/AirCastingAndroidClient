package pl.llp.aircasting.model.events;

import pl.llp.aircasting.model.RealtimeSession;

public class RealtimeMeasurementEvent
{
  private RealtimeSession realtimeSession;

  public RealtimeMeasurementEvent(RealtimeSession realtimeSession)
  {
    this.realtimeSession = realtimeSession;
  }

  public RealtimeSession getRealtimeSession()
  {
    return realtimeSession;
  }
}
