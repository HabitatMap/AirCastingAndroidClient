package pl.llp.aircasting.activity.adapter;

import android.content.Context;
import com.google.inject.Inject;
import pl.llp.aircasting.model.Regression;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.storage.repository.RegressionRepository;

import java.util.List;

/**
 * Created by marcin on 28/07/14.
 */
public class RegressionAdapterFactory {
    @Inject RegressionRepository regressionRepository;
    @Inject SessionManager sessionManager;
    @Inject SensorManager sensorManager;

    public RegressionAdapter create(Context context) {
        List<Regression> regressions = regressionRepository.forSensors(sensorManager.getSensors());
        return new RegressionAdapter(context, regressions, regressionRepository, sessionManager);
    }
}
