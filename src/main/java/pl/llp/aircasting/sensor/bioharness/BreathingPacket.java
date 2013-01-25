package pl.llp.aircasting.sensor.bioharness;

public class BreathingPacket
{
  static class EnablePacket
  {
    public static byte[] getRequest(Packet.Request request)
    {
      byte[] bytes = {PacketType.STX, 22, 1, 0, 0, PacketType.ETX};
      bytes[3] = (byte) (request.isEnabled() ? 1 : 0);
      return bytes;
    }
  }
}
