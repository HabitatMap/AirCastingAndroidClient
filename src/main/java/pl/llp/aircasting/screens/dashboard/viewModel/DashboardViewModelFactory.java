package pl.llp.aircasting.screens.dashboard.viewModel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {
    private final CurrentSessionManager mCurrentSessionManager;
    private final CurrentSessionSensorManager mCurrentSessionSensorManager;
    private final ApplicationState mState;

    public DashboardViewModelFactory(CurrentSessionManager currentSessionManager, CurrentSessionSensorManager currentSessionSensorManager, ApplicationState applicationState) {
        this.mCurrentSessionManager = currentSessionManager;
        this.mCurrentSessionSensorManager = currentSessionSensorManager;
        this.mState = applicationState;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(mCurrentSessionManager, mCurrentSessionSensorManager, mState);
        }
        throw new IllegalArgumentException("Unknown ViewModel class.");
    }
}
