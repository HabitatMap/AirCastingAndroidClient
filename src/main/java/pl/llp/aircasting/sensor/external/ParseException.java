package pl.llp.aircasting.sensor.external;

public class ParseException extends Exception {
    public ParseException(Exception inner) {
        super(inner);
    }

    public ParseException(String message) {
        super(message);
    }
}
