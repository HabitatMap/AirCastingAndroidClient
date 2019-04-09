package pl.llp.aircasting.screens.dashboard.viewModel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsSensorManager;
import pl.llp.aircasting.screens.dashboard.DashboardChartManager;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {
    @Inject CurrentSessionManager mCurrentSessionManager;
    @Inject CurrentSessionSensorManager mCurrentSessionSensorManager;
    @Inject ViewingSessionsManager mViewingSessionsManager;
    @Inject ViewingSessionsSensorManager mViewingSessionsSensorManager;
    @Inject DashboardChartManager mDashboardChartManager;
    @Inject ApplicationState mState;

    public DashboardViewModelFactory() {}

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(mCurrentSessionManager, mCurrentSessionSensorManager, mViewingSessionsManager, mViewingSessionsSensorManager, mDashboardChartManager, mState);
        }
        throw new IllegalArgumentException("Unknown ViewModel class.");
    }
}
