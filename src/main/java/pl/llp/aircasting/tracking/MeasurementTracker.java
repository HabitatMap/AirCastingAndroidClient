package pl.llp.aircasting.tracking;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;

/**
 * Created by ags on 24/03/2013 at 22:21
 */
public interface MeasurementTracker
{
  void add(MeasurementStream stream, Measurement measurement);
}
