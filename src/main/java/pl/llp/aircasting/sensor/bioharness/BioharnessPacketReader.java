package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.util.Pair;

import com.google.common.eventbus.EventBus;

import java.io.ByteArrayOutputStream;

import static pl.llp.aircasting.sensor.bioharness.BioharnessPacketReader.buildBioharnessEvent;

public class BioharnessPacketReader
{
  private final TimestampTracker timestampTracker = new TimestampTracker();
  private final EventBus eventBus;
  private final RtoRPacketReader rToRReader;

  public BioharnessPacketReader(EventBus eventBus)
  {
    this.eventBus = eventBus;
    this.rToRReader = new RtoRPacketReader(eventBus, timestampTracker);
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
      if (data.length - (length + offset) < 0)
      {
        continue;
      }

      switch (packetType)
      {
        case SummaryPacket:
        {
          SummaryPacket packet = new SummaryPacket(data, offset);
          long timeStamp = timestampTracker.getLocalTimestamp(packet.getTimeStamp());
          postHeartRate(packet, timeStamp);
          postBreathing(packet);
          postCoreTemperature(packet);
          postActivity(packet);
          postAcceleration(packet);
          break;
        }
        case RtoRPacket:
        {
          rToRReader.process(data, offset);
          break;
        }
        case ECGPacket:
        {
          break;
        }
      }

      return offset + length;
    }
    return 0;
  }

  void postRtoR(RtoRPacket packet, long timeStamp)
  {
    int[] samples = packet.getSamples();
    int previous = Integer.MIN_VALUE;
    for (int i = 0; i < samples.length; i++)
    {
      int value = samples[i];
      int absValue = Math.abs(value);
      if (0 < absValue && absValue <= 3000 && absValue != previous)
      {
        postRtoREvent(absValue, timeStamp + i * 56);
      }
      previous = absValue;
    }
  }

  void postRtoREvent(int value, long timeStamp)
  {
    SensorEvent event = buildBioharnessEvent("R to R", "RTR", "milliseconds", "ms", 400, 800, 1200, 1600, 2000, value, timeStamp);
    eventBus.post(event);
  }

  private void postCoreTemperature(SummaryPacket packet)
  {
    if (packet.isCoreTemperatureReliable())
    {
      double coreTemperature = packet.getCoreTemperature();
      SensorEvent event = buildBioharnessEvent("Core Temperature", "CT", "degrees Celsius", "C", 36, 37, 38, 39, 40, coreTemperature);
      eventBus.post(event);
    }
  }

  private void postAcceleration(SummaryPacket packet)
  {
    SensorEvent event = buildBioharnessEvent("Peak Acceleration", "PkA", "standard gravity", ".01g", 0, 100, 200, 300, 400, packet
        .getPeakAcceleration());
    eventBus.post(event);
  }

  private void postActivity(SummaryPacket packet)
  {
    if (packet.isActivityReliable())
    {
      double value = packet.getActivity();
      SensorEvent event = buildBioharnessEvent("Activity Level", "AL", "Vector Magnitude Units", "VMU", 0, 50, 100, 150, 200, value);
      eventBus.post(event);
    }
  }

  void postHeartRate(SummaryPacket packet, long timestamp)
  {
    SensorEvent event;
    if (packet.isHeartRateReliable())
    {
      int heartRate = packet.getHeartRate();
      event = buildBioharnessEvent("Heart Rate", "HR", "beats per minute", "bpm", 40, 85, 130, 175, 220, heartRate);
      eventBus.post(event);
    }
    if (packet.isHeartRateVariabilityReliable())
    {
      int variability = packet.getHeartRateVariability();
      event = buildBioharnessEvent("Heart Rate Variability", "HRV", "milliseconds", "ms", 0, 70, 140, 210, 280, variability, timestamp);
      eventBus.post(event);
    }
  }

  void postBreathing(SummaryPacket packet)
  {
    if (packet.isRespirationRateReliable())
    {
      double respirationRate = packet.getRespirationRate();
      SensorEvent event = buildBioharnessEvent("Breathing Rate", "BR", "breaths per minute", "bpm", 0, 30, 60, 90, 120, respirationRate);
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
    return new SensorEvent("BioHarness3", "BioHarness3:" + shortName, longName, shortName, unitLong, unitShort,
                           thresholdVeryLow,
                           thresholdLow,
                           thresholdMedium, thresholdHigh, thresholdVeryHigh, value);
  }

  static SensorEvent buildBioharnessEvent(String longName,
                                   String shortName,
                                   String unitLong,
                                   String unitShort,
                                   int thresholdVeryLow,
                                   int thresholdLow,
                                   int thresholdMedium,
                                   int thresholdHigh,
                                   int thresholdVeryHigh,
                                   double value,
                                   long timeStamp
                                  )
  {
    return new SensorEvent("BioHarness3", "BioHarness3:" + shortName, longName, shortName, unitLong, unitShort,
                           thresholdVeryLow,
                           thresholdLow,
                           thresholdMedium, thresholdHigh, thresholdVeryHigh, value, timeStamp);
  }
}

class RtoRPacketReader
{
  private final RepeatedValueTracker repeatedTracker = new RepeatedValueTracker();

  private final EventBus eventBus;
  private final TimestampTracker timestampTracker;

  public RtoRPacketReader(EventBus eventBus, TimestampTracker timestampTracker)
  {
    this.eventBus = eventBus;
    this.timestampTracker = timestampTracker;
  }

  void postRtoR(RtoRPacket packet, long timeStamp)
  {
    int[] samples = packet.getSamples();
    for (int i = 0; i < samples.length; i++)
    {
      int value = samples[i];
      if (-3000 <= value && value <= 3000)
      {
        if(repeatedTracker.isNew(value))
        {
          int absValue = Math.abs(value);
          postRtoREvent(absValue, timeStamp + i * 56);
        }
      }
    }
  }

  void postRtoREvent(int value, long timeStamp)
  {
    SensorEvent event = buildBioharnessEvent("R to R", "RTR", "milliseconds", "ms", 400, 800, 1200, 1600, 2000, value, timeStamp);
    eventBus.post(event);
  }

  public void process(byte[] data, int offset)
  {
    RtoRPacket packet = new RtoRPacket(data, offset);
    long timeStamp = timestampTracker.getLocalTimestamp(packet.getTimeStamp());
    postRtoR(packet, timeStamp);
  }
}

class RepeatedValueTracker
{
  boolean first = true;
  int previous = 0;

  boolean isNew(int value)
  {
    if (first)
    {
      first = false;
      previous = value;
      return true;
    }

    boolean result = previous != value;
    previous = value;
    return result;
  }
}