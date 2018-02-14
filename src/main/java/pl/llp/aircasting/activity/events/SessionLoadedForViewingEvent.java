package pl.llp.aircasting.activity.events;

import pl.llp.aircasting.model.Session;

/**
 * Created by ags on 08/08/12 at 19:52
 */
public class SessionLoadedForViewingEvent {
    private final Session session;
    private boolean newFixedSession = false;

    public SessionLoadedForViewingEvent(Session session, boolean newFixedSession) {
        this.session = session;
        this.newFixedSession = newFixedSession;
    }

    public Session getSession() {
        return session;
    }

    public boolean isNewFixedSession() {
        return newFixedSession;
    }
}
