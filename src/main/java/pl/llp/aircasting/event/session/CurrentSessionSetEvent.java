package pl.llp.aircasting.event.session;

import pl.llp.aircasting.model.Session;

/**
 * Created by radek on 03/11/17.
 */
public class CurrentSessionSetEvent {
    private Session session;

    public CurrentSessionSetEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
