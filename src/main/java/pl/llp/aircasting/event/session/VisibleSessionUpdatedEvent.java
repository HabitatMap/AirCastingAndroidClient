package pl.llp.aircasting.event.session;

import pl.llp.aircasting.model.Session;

public class VisibleSessionUpdatedEvent
{
  private final Session session;
  private final Throwable origin;

  public VisibleSessionUpdatedEvent(Session session)
  {
    this.origin = new Throwable();
    this.session = session;
  }

  public Session getSession()
  {
    return session;
  }
}
