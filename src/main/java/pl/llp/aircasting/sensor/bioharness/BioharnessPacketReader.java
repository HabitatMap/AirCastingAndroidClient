package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.util.Pair;

import com.google.common.eventbus.EventBus;

import java.io.ByteArrayOutputStream;

class BioharnessPacketReader
{
  private final EventBus eventBus;

  public BioharnessPacketReader(EventBus eventBus)
  {
    this.eventBus = eventBus;
  }

  public Integer tryReading(ByteArrayOutputStream input)
  {
    Pair<PacketType, Integer> typeAndLength;
    byte[] data = input.toByteArray();
    for (int offset = 0; offset < data.length; offset++)
    {
      typeAndLength = PacketType.decide(data, offset);
      PacketType packetType = typeAndLength.getFirst();

      if (!packetType.isValid())
      {
        continue;
      }

      Integer length = typeAndLength.getSecond();
      if(data.length - (length + offset) < 0)
      {
        continue;
      }

      switch (packetType)
      {
        case GeneralPacket:
          GeneralPacket packet = new GeneralPacket(data, offset);
          postHeartRate(packet);
          postSkinTemperature(packet);
          postBreathing(packet);
          break;
        case Lifesign:

          break;
      }

      return offset + length;
    }
    return 0;
  }

  void postHeartRate(GeneralPacket packet)
  {
    if(packet.isHeartRateReliable())
    {
      int heartRate = packet.getHeartRate();
      SensorEvent event = buildBioharnessEvent("Heart Rate", "HR", "beats per minute", "bpm", 40, 85, 130, 175, 220, heartRate);
      eventBus.post(event);
    }
    if(packet.isHeartRateVariabilityReliable())
    {
      int variability = packet.getHeartRateVariability();
      SensorEvent event = buildBioharnessEvent("Heart Rate Variability", "HRV", "variability", "v", 40, 85, 130, 175, 220, variability);
      eventBus.post(event);
    }
  }

  void postBreathing(GeneralPacket packet)
  {
    if(packet.isRespirationRateReliable())
    {
      double respirationRate = packet.getRespirationRate();
      SensorEvent event = buildBioharnessEvent("Breathing", "something", "breaths per minute", "bs", 5, 10, 20, 36, 40, respirationRate);
      eventBus.post(event);
    }
  }

  void postSkinTemperature(GeneralPacket packet)
  {
    if(packet.isSkinTemperatureReliable())
    {
      double skinTemperature = packet.getSkinTemperature();
      SensorEvent event = buildBioharnessEvent("Skin temperature", "skin", "C", "C", 5, 10, 20, 36, 40, skinTemperature);
      eventBus.post(event);
    }
  }

  SensorEvent buildBioharnessEvent(String longName,
                                   String shortName,
                                   String unitLong,
                                   String unitShort,
                                   int thresholdVeryLow,
                                   int thresholdLow,
                                   int thresholdMedium,
                                   int thresholdHigh,
                                   int thresholdVeryHigh,
                                   double value
                                  )
  {
    return new SensorEvent("Zephyr", "Bioharness", longName, shortName, unitLong, unitShort,
                           thresholdVeryLow,
                           thresholdLow,
                           thresholdMedium, thresholdHigh, thresholdVeryHigh, value);
  }
}
