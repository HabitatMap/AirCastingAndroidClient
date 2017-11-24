package pl.llp.aircasting.activity.events;

/**
 * Created by ags on 08/08/12 at 19:52
 */
public class SessionSensorsLoadedEvent {
    private final long sessionId;

    public SessionSensorsLoadedEvent(long sessionId)
    {
        this.sessionId = sessionId;
    }

    public long getSessionId()
    {
        return sessionId;
    }
}
