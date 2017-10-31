package pl.llp.aircasting.event.session;

import pl.llp.aircasting.model.Session;

/**
 * Created by radek on 20/10/17.
 */
public class ViewSessionEvent {
    private Session session;

    public ViewSessionEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
