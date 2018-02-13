package pl.llp.aircasting.sensor.airbeam;

import android.util.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.model.ViewingSessionsManager;
import pl.llp.aircasting.util.base64.Base64;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by radek on 13/12/17.
 */
@Singleton
public class Airbeam2Configurator {
    @Inject EventBus eventBus;
    @Inject ViewingSessionsManager viewingSessionsManager;

    private static byte streamingMethod;

    private static final byte BEGIN_MESSAGE = (byte) 0xfe;
    private static final byte END_MESSAGE = (byte) 0xff;
    private static final byte BLUETOOTH_CODE = (byte) 0x01;
    private static final byte WIFI_CODE = (byte) 0x02;
    private static final byte CELLULAR_CODE = (byte) 0x03;
    private static final byte UUID_CODE = (byte) 0x04;
    private static final byte AUTH_TOKEN_CODE = (byte) 0x05;
    private static final byte LAT_LNG_CODE = (byte) 0x06;
    // for testing purposes only
    private static final byte SENSOR_PACKAGE_CODE = (byte) 0x07;
    private String wifiSSID;
    private String wifiPassword;

    private static final byte[] BLUETOOTH_CONFIGURATION_MESSAGE = new byte[] { BEGIN_MESSAGE, BLUETOOTH_CODE, END_MESSAGE };
    private static final byte[] CELLULAR_CONFIGURATION_MESSAGE = new byte[] { BEGIN_MESSAGE, CELLULAR_CODE, END_MESSAGE };

    public void configureBluetooth() {
        sendMessage(BLUETOOTH_CONFIGURATION_MESSAGE);
    }
  
    public void setCellular() {
        streamingMethod = CELLULAR_CODE;
    }

    public void setWifi(String ssid, String password) {
        streamingMethod = WIFI_CODE;
        wifiSSID = ssid;
        wifiPassword = password;
    }

    public void sendUUID(UUID uuid) {
        String rawUUIDString = uuid.toString();
        byte[] uuidMessage = buildMessage(rawUUIDString, UUID_CODE);

        sendMessage(uuidMessage);
    }

    public void sendAuthToken(String authToken) {
        byte[] data = new byte[0];

        String rawAuthToken = authToken + ":X";

        try {
            data = rawAuthToken.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        String base64 = Base64.encodeBase64String(data);

        byte[] authTokenMessage = buildMessage(base64, AUTH_TOKEN_CODE);

        sendMessage(authTokenMessage);
    }

    public void sendFinalAb2Config() {
        sendLatLng();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendPackageName();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        configureStreamingMethod();
    }

    public void sendLatLng() {
        double lat = viewingSessionsManager.getStreamingSession().getLatitude();
        double lng = viewingSessionsManager.getStreamingSession().getLongitude();

        if (lat == 0) {
            lat = 200;
        }

        if (lng == 0) {
            lng = 200;
        }

        String rawLatLngStr = lat + "," + lng;
        byte[] latLngMessage = buildMessage(rawLatLngStr, LAT_LNG_CODE);

        sendMessage(latLngMessage);
    }

    public void configureStreamingMethod() {
        switch(streamingMethod) {
            case CELLULAR_CODE:
                sendMessage(CELLULAR_CONFIGURATION_MESSAGE);
                break;
            case WIFI_CODE:
                int GMTOffset = getTimezoneOffsetInHours();
                String rawWifiConfigStr = wifiSSID + "," + wifiPassword + "," + GMTOffset;
                byte[] wifiConfigMessage = buildMessage(rawWifiConfigStr, WIFI_CODE);

                sendMessage(wifiConfigMessage);
                break;
        }
    }

    private byte[] buildMessage(String messageString, byte configurationCode) {
        String hexString = asciiToHex(messageString);
        ArrayList<Byte> messageList = hexStringToByteList(hexString, configurationCode);

        return byteListToArray(messageList);
    }

    private static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();

        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();
    }

    private ArrayList<Byte> hexStringToByteList(String s, byte configurationCode) {
        int len = s.length();
        ArrayList<Byte> message = new ArrayList<Byte>();
        message.add(BEGIN_MESSAGE);
        message.add(configurationCode);

        for(int i = 0; i < len; i+=2){
            message.add((byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16)));
        }

        message.add(END_MESSAGE);

        return message;
    }

    private byte[] byteListToArray(ArrayList<Byte> messageList) {
        byte[] message = new byte[messageList.size()];

        for (int i = 0; i < messageList.size(); i++) {
            message[i] = messageList.get(i);
        }

        return message;
    }

    private int getTimezoneOffsetInHours() {
        Calendar calendar = new GregorianCalendar();
        TimeZone timeZone = calendar.getTimeZone();
        int GMTOffset = timeZone.getRawOffset();
        int offsetInHours = (int) TimeUnit.HOURS.convert(GMTOffset, TimeUnit.MILLISECONDS);

        return offsetInHours;
    }

    private void sendMessage(byte[] message) {
        eventBus.post(new Airbeam2ConfigMessageEvent(message));
    }

    public void sendPackageName() {
        String rawPackageNameStr = "AirBeam:";
        byte[] message = buildMessage(rawPackageNameStr, SENSOR_PACKAGE_CODE);

        sendMessage(message);
    }

    public void sendUUIDAndAuthToken(UUID uuid, String authToken) {
        sendUUID(uuid);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendAuthToken(authToken);
    }
}
