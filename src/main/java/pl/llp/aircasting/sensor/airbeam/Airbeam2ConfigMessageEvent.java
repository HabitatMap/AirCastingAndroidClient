package pl.llp.aircasting.sensor.airbeam;

/**
 * Created by radek on 08/12/17.
 */
public class Airbeam2ConfigMessageEvent {
    private byte[] message;

    public Airbeam2ConfigMessageEvent(byte[] message) {
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }
}
