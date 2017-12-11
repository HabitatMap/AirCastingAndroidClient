package pl.llp.aircasting.sensor;

import pl.llp.aircasting.util.Constants;

import com.google.common.eventbus.EventBus;

public abstract class Worker {
    private static final long MAX_CONNECTION_FAILURE_TIME = Constants.ONE_MINUTE;

    protected final EventBus eventBus;

    private volatile long connectionFailingSince = -1;
    Status status = Status.NOT_YET_STARTED;

    Worker(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected void considerStoppingOnFailure() {
        long currentTime = System.currentTimeMillis();
        if (connectionFailingSince < 0) {
            connectionFailingSince = currentTime;
        } else {
            long difference = currentTime - connectionFailingSince;
            if (difference > MAX_CONNECTION_FAILURE_TIME) {
                handlePersistentFailure();
                stop();
            }
        }
    }

    public void start() {
        status = Status.STARTED;
        customStart();
    }

    public void stop() {
        status = Status.STOPPED;
        customStop();
    }

    public abstract void customStart();

    public abstract void customStop();

    public abstract void handlePersistentFailure();
}
