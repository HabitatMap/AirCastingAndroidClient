package pl.llp.aircasting.sensor.hxm;

/**
 * Created by ags on 04/09/12 at 13:43
 */
public class HxMPacket
{
  private String firmwareID;
  private String firmwareVersion;
  private String hardwareID;
  private String hardwareVersion;
  private byte batteryChargeInd;
  private byte heartRate;
  private byte heartBeatNum;
  private int[] heartBeatTimeStamp = new int[15];
  private double distance;
  private double instantSpeed;
  private byte strides;

  public static int getHeartRate(byte[] data)
  {
    return data[12] & 0xFF;
  }
}
