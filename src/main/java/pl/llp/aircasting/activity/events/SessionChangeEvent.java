package pl.llp.aircasting.activity.events;

import pl.llp.aircasting.model.Session;

public class SessionChangeEvent
{
  private final Session session;
  private final Throwable origin;

  public SessionChangeEvent(Session session)
  {
    this.origin = new Throwable();
    this.session = session;
  }

  public Session getSession()
  {
    return session;
  }
}
