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
        case SummaryPacket:
          SummaryPacket packet = new SummaryPacket(data, offset);
          postHeartRate(packet);
          postSkinTemperature(packet);
          postBreathing(packet);
          postCoreTemperature(packet);
          postGSR(packet);
          break;
        case Lifesign:

          break;
      }

      return offset + length;
    }
    return 0;
  }

  private void postGSR(SummaryPacket packet)
  {
    if(packet.isGSRReliable())
    {
      int galvanicSkinResponse = packet.getGalvanicSkinResponse();
      SensorEvent event = buildBioharnessEvent("Galvanic Skin Response", "GSR", "microsiemens", "uS", 0, 10, 100, 1000, 10000, galvanicSkinResponse);
      eventBus.post(event);
    }
  }

  private void postCoreTemperature(SummaryPacket packet)
  {
    if(packet.isCoreTemperatureReliable())
    {
      double coreTemperature = packet.getCoreTemperature();
      SensorEvent event = buildBioharnessEvent("Core Temperature", "CT", "degrees Celsius", "C", 33, 35, 37, 39, 41, coreTemperature);
      eventBus.post(event);
    }
  }

  void postHeartRate(SummaryPacket packet)
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
      SensorEvent event = buildBioharnessEvent("Heart Rate Variability", "HRV", "milliseconds", "ms", 0, 70, 140, 210, 280, variability);
      eventBus.post(event);
    }
  }

  void postBreathing(SummaryPacket packet)
  {
    if(packet.isRespirationRateReliable())
    {
      double respirationRate = packet.getRespirationRate();
      SensorEvent event = buildBioharnessEvent("Breathing Rate", "BR", "breaths per minute", "bpm", 0, 30, 60, 90, 120, respirationRate);
      eventBus.post(event);
    }
  }

  void postSkinTemperature(SummaryPacket packet)
  {
    if(packet.isSkinTemperatureReliable())
    {
      double skinTemperature = packet.getSkinTemperature();
      SensorEvent event = buildBioharnessEvent("Skin temperature", "ST", "Degrees Celsius", "C", 10, 20, 30, 40, 50, skinTemperature);
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
    return new SensorEvent("BioHarness3", "BioHarness3-" + shortName, longName, shortName, unitLong, unitShort,
                           thresholdVeryLow,
                           thresholdLow,
                           thresholdMedium, thresholdHigh, thresholdVeryHigh, value);
  }
}
