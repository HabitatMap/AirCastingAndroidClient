package pl.llp.aircasting.activity;

import com.google.inject.Singleton;

/**
 * Created by ags on 17/04/2013 at 07:19
 */
@Singleton
public class ApplicationState {
    public final SavingState saving = new SavingState();
    public final RecordingState recording = new RecordingState();
    public final SensorState sensorState = new SensorState();
    public final MicrophoneState microphoneState = new MicrophoneState();
    public final DashboardState dashboardState = new DashboardState();

    public RecordingState recording() {
        return recording;
    }

    public SavingState saving() {
        return saving;
    }

    public SensorState sensors() {
        return sensorState;
    }

    public MicrophoneState microphoneState() { return microphoneState; }

    public DashboardState dashboardState() { return dashboardState; }

    @Override
    public String toString() {
        return "ApplicationState{" +
                "saving=" + saving +
                ", recording=" + recording +
                '}';
    }

    public static class SensorState {
        States state;

        public void start() {
            state = States.STARTED;
        }

        public boolean started() {
            return States.STARTED.equals(state);
        }

        public void stop() {
            this.state = States.STOPPED;
        }

        enum States {
            STARTED,
            STOPPED,
        }
    }

    public static class MicrophoneState extends SensorState {}

    public static class DashboardState {
        DashboardListStates state = DashboardListStates.EMPTY;

        public boolean isPopulated() { return state == DashboardListStates.POPULATED; }

        public void populate() { state = DashboardListStates.POPULATED; }

        public void empty() { state = DashboardListStates.EMPTY; }

        enum DashboardListStates {
            POPULATED,
            EMPTY
        }
    }

    public static class RecordingState {
        CurrentSessionState state = CurrentSessionState.SHOWING_LIVE_DATA;

        public boolean isJustShowingCurrentValues() {
            return state == CurrentSessionState.SHOWING_LIVE_DATA;
        }

        public boolean isShowingASession() {
            return !isJustShowingCurrentValues();
        }

        public void startShowingOldSession() {
            state = CurrentSessionState.PLAYBACK;
        }

        public void stopRecording() {
            state = CurrentSessionState.SHOWING_LIVE_DATA;
        }

        public void startRecording() {
            state = CurrentSessionState.RECORDING;
        }

        enum CurrentSessionState {
            RECORDING,
            PLAYBACK,
            SHOWING_LIVE_DATA
        }

        public boolean isShowingOldSession() {
            return state == CurrentSessionState.PLAYBACK;
        }

        public boolean isRecording() {
            return state == CurrentSessionState.RECORDING;
        }

        @Override
        public String toString() {
            return "RecordingState{" +
                    "state=" + state +
                    '}';
        }
    }
}
