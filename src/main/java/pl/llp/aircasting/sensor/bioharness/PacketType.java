package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.util.Pair;

public enum PacketType
{
  GeneralPacket(0x20)
      {
        @Override
        public byte[] getRequest(Packet.Request request)
        {
          byte[] bytes = {2, 20, 1, 0, 0, 3};
          if (request.isEnabled())
          {
            bytes[3] = (byte) 1;
            bytes[4] = 94;
          }
          else
          {
            bytes[3] = (byte) 0;
            bytes[4] = 0;
          }
          return bytes;
        }
      },
  BreathingPacket(0x21)
      {
        @Override
        public byte[] getRequest(Packet.Request request)
        {
          byte[] bytes = { 2, 21, 1, 0, 0, 3 };

          if (request.isEnabled())
          {
            bytes[3] = 1;
            bytes[4] = 94;
          }
          return bytes;
        }
      },
  ECGPacket(0x22)
      {
        @Override
        public byte[] getRequest(Packet.Request request)
        {
          byte[] bytes = { 2, 22, 1, 0, 0, 3 };

          if (request.isEnabled())
          {
            bytes[3] = 1;
            bytes[4] = 94;
          }
          return bytes;
        }
      },
  Lifesign(0x23)
      {
        @Override
        public byte[] getRequest(Packet.Request request)
        {
          throw new RuntimeException("Not implemented yet");
        }
      },
  RtoR(0x24)
      {
        @Override
        public byte[] getRequest(Packet.Request request)
        {
          byte[] bytes = { 2, 25, 1, 0, 0, 3 };

          if (request.isEnabled())
          {
            bytes[3] = 1;
            bytes[4] = 94;
          }
          return bytes;
        }
      },
  SummaryPacket(0x2B)
      {
        @Override
        public byte[] getRequest(Packet.Request request)
        {
          byte[] bytes = {2, -67, 2, 0, 0, 0, 3};
          if (request.isEnabled())
          {
            bytes[3] = (byte) 1;
            bytes[5] = -60;
          }
          return bytes;
        }
      },
  Invalid(0x00, false)
      {
        @Override
        public byte[] getRequest(Packet.Request request)
        {
          throw new RuntimeException("Should not be called");
        }
      },
  ;

  static final byte STX = 0x02;
  static final byte ETX = 0x03;

  private boolean valid = true;
  public final int messageId;

  public abstract byte[] getRequest(Packet.Request request);

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
      if (packetType == PacketType.Invalid)
      {
        return packetAndLenght(PacketType.Invalid, 0);
      }

      if (packetType == Lifesign)
      {
        return packetAndLenght(Lifesign, 5);
      }

      int length = input[index + 2];
      if ((index + length) > input.length)
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
      case 0x24:
        return RtoR;
      case 0x2B:
        return SummaryPacket;
    }
    return PacketType.Invalid;
  }

  public boolean isValid()
  {
    return valid;
  }
}
