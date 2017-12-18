package pl.llp.aircasting.sensor.airbeam;

import java.util.UUID;

/**
 * Created by radek on 13/12/17.
 */
public class HexTranslator {
    public static final byte[] BLUETOOTH_CONFIGURATION_MESSAGE = new byte[] { (byte) 0xfe, (byte) 0x01, (byte) 0xff, (byte) '\n' };

    public static void sendUUIDAndAuthToken(UUID uuid, String authToken) {
    }
}
