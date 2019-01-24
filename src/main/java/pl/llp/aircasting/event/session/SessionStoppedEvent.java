package pl.llp.aircasting.event.session;

import pl.llp.aircasting.model.Session;

/**
 * Created by ags on 08/08/12 at 19:43
 */
public class SessionStoppedEvent
{
  private final Session session;

  public SessionStoppedEvent(Session session)
  {
    this.session = session;
  }

  public Session getSession()
  {
    return session;
  }
}
