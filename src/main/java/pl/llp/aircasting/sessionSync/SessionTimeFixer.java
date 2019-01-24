package pl.llp.aircasting.sessionSync;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;

import java.util.Date;
import java.util.List;

public class SessionTimeFixer {
    public void fromUTCtoLocal(Session session) {
        if (needsTimeFix(session)) {
            fixStartEndTimeFromMeasurements(session);
        }

        int minutes = getOffset(session);
        int offset = minutes * 60 * 1000;

        Date start = session.getStart();
        Date end = session.getEnd();
        session.setStart(new Date(start.getTime() + offset));
        session.setEnd(new Date(end.getTime() + offset));

        List<MeasurementStream> streams = session.getActiveMeasurementStreams();
        for (MeasurementStream stream : streams) {
            List<Measurement> measurements = stream.getMeasurements();
            for (Measurement measurement : measurements) {
                measurement.setTime(new Date(measurement.getTime().getTime() + offset));
            }
        }
    }

    boolean needsTimeFix(Session session) {
        return session.getStart() == null || session.getEnd() == null;
    }

    private int getOffset(Session session) {
        List<MeasurementStream> streams = session.getActiveMeasurementStreams();
        for (MeasurementStream stream : streams) {
            List<Measurement> measurements = stream.getMeasurements();
            for (Measurement measurement : measurements) {
                return measurement.getTimeZoneOffsetMinutes();
            }
        }
        return 0;
    }

    void fixStartEndTimeFromMeasurements(Session session) {
        Date start = session.getStart();
        Date end = session.getEnd();

        for (MeasurementStream stream : session.getMeasurementStreams()) {
            List<Measurement> measurements = stream.getMeasurements();
            if (measurements.isEmpty()) {
                continue;
            }

            Measurement first = measurements.get(0);
            Measurement last = measurements.get(measurements.size() - 1);

            if (start == null) {
                start = first.getTime();
            } else {
                start = start.before(first.getTime()) ? start : first.getTime();
            }

            if (end == null) {
                end = last.getTime();
            } else {
                end = end.before(last.getTime()) ? last.getTime() : end;
            }
        }

        if (start == null || end == null) {
            String message = "Session [" + session.getId() + "] has incorrect start/end date [" + start + "/" + end + "]";
            throw new SessionSyncException(message);
        }

        session.setStart(new Date(start.getTime()));
        session.setEnd(new Date(end.getTime()));
    }
}
