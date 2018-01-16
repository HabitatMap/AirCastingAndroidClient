package pl.llp.aircasting.activity.events;

import pl.llp.aircasting.model.Session;

/**
 * Created by ags on 08/08/12 at 19:52
 */
public class SessionLoadedForViewingEvent {
    private final Session session;

    public SessionLoadedForViewingEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
