package pl.llp.aircasting.sensor.hxm;

/**
 * Created by ags on 04/09/12 at 14:37
 */
public class CRCCheck
{
  public static final int BASE = 0x8C;

  public static byte calculate(byte[] bytes)
  {
    int crc = 0;

    for (byte aByte : bytes)
    {
      crc = (crc ^ aByte) & 0xFF;
      for (int loop = 0; loop < 8; loop++)
      {
        if ((crc & 0x1) == 1)
        {
          crc = crc >> 1 ^ BASE;
        }
        else
        {
          crc >>= 1;
        }
      }
      crc &= 255;
    }

    return (byte)crc;
  }
}
