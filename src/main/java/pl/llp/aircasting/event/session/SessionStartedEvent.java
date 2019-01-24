package pl.llp.aircasting.event.session;

import pl.llp.aircasting.model.Session;

/**
 * Created by ags on 08/08/12 at 19:42
 */
public class SessionStartedEvent
{
  private final Session session;

  public SessionStartedEvent(Session session)
  {
    this.session = session;
  }

  public Session getSession()
  {
    return session;
  }
}
