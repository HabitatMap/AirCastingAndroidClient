package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.util.Pair;

public enum PacketType
{
  GeneralPacket(0x20),
  BreathingPacket(0x21),
  ECGPacket(0x22),
  Lifesign(0x23),
  Invalid(0x00, false),
  ;

  static final byte STX = 0x02;
  static final byte ETX = 0x03;

  private boolean valid = true;
  private final int messageId;

  PacketType(int id)
  {
    this.messageId = id;
  }

  PacketType(int id, boolean valid)
  {
    this(id);
    this.valid = valid;
  }

  public static Pair<PacketType, Integer> decide(byte[] input, int index)
  {
    if (input[index] == STX)
    {
      int messageId = input[index + 1];
      PacketType packetType = decode(messageId);
      if(packetType == PacketType.Invalid)
      {
        return packetAndLenght(PacketType.Invalid, 0);
      }

      if(packetType == Lifesign)
      {
        return packetAndLenght(Lifesign, 5);
      }

      int length = input[index + 2];
      if((index + length) > input.length)
      {
        return packetAndLenght(PacketType.Invalid, 0);
      }

      if (input[index + 2 + length + 2] == ETX)
      {
        return packetAndLenght(packetType, length);
      }
    }
    return packetAndLenght(PacketType.Invalid, 0);
  }

  public static Pair<PacketType, Integer> packetAndLenght(PacketType packetType, int length)
  {
    return new Pair<PacketType, Integer>(packetType, length);
  }

  public static PacketType decode(int messageId)
  {
    switch (messageId)
    {
      case 0x20:
        return PacketType.GeneralPacket;
      case 0x21:
        return PacketType.BreathingPacket;
      case 0x22:
        return PacketType.ECGPacket;
      case 0x23:
        return PacketType.Lifesign;
    }
    return PacketType.Invalid;
  }

  public boolean isValid()
  {
    return valid;
  }

  public static class BreathingPacket extends Packet
  {
    private final byte[] base;

    BreathingPacket(byte[] input)
    {
      this.base = input;
    }

    public static byte[] getRequest(boolean enabled)
    {
      byte[] bytes = {PacketType.STX, 22, 1, 0, 0, PacketType.ETX};
      bytes[3] = (byte) (enabled ? 1 : 0);
      return bytes;
    }
  }
}
