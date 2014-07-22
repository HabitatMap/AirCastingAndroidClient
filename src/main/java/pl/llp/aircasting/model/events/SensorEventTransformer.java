package pl.llp.aircasting.model.events;

import com.google.inject.Inject;
import pl.llp.aircasting.model.Regression;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.storage.repository.RegressionRepository;

/**
 * Created by marcin on 22/07/14.
 */
public class SensorEventTransformer {

    @Inject RegressionRepository regressionRepository;

    public SensorEvent transform(SensorEvent event) {

        Regression regression = regressionRepository.getForSensor(event.getSensorName(), event.getPackageName());
        if (regression == null) return event;

        SensorEvent e = new SensorEvent(event.getPackageName(), event.getSensorName(), regression.getMeasurementType(),
                event.getShortType(), regression.getMeasurementUnit(), regression.getMeasurementSymbol(),
                regression.getThresholdVeryLow(), regression.getThresholdLow(), regression.getThresholdMedium(),
                regression.getThresholdHigh(), regression.getThresholdVeryHigh(), regression.apply(event.getValue()),
                event.getValue());
        e.setAddress(event.getAddress());
        e.setDate(event.getDate());
        return e;
    }
}
