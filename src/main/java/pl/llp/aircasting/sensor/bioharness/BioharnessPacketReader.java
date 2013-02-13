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
        {
          SummaryPacket packet = new SummaryPacket(data, offset);
          postHeartRate(packet);
          postBreathing(packet);
          postCoreTemperature(packet);
          break;
        }
        case RtoRPacket:
        {
          RtoRPacket packet = new RtoRPacket(data, offset);
          postRtoR(packet);
          break;
        }
        case ECGPacket:
        {
          ECGPacket packet = new ECGPacket(data, offset);
          postECGWave(packet);
          break;
        }
      }

      return offset + length;
    }
    return 0;
  }

  private void postECGWave(ECGPacket packet)
  {
    int[] samples = packet.getSamples();
    long timeStamp = System.currentTimeMillis();
    for (int i = 0; i < samples.length; i++)
    {
      int value = samples[i];
      postECGWaveEvent(value, timeStamp + i * 4);
    }
  }

  private void postRtoR(RtoRPacket packet)
  {
    int[] samples = packet.getSamples();
    long timeStamp = System.currentTimeMillis();
    for (int i = 0; i < samples.length; i++)
    {
      int value = samples[i];
      postRtoREvent(value, timeStamp + i * 56);
    }
  }

  private void postECGWaveEvent(int value, long timeStamp)
  {
    SensorEvent event = buildBioharnessEvent("ECG Wave", "ECG", "millivolts", "mV", 0, 300, 600, 900, 1200, value, timeStamp);
    eventBus.post(event);
  }

  private void postRtoREvent(int value, long timeStamp)
  {
    SensorEvent event = buildBioharnessEvent("R to R", "RTR", "milliseconds", "ms", 0, 10, 100, 1000, 25000, value, timeStamp);
    eventBus.post(event);
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
    SensorEvent event;
    if(packet.isHeartRateReliable())
    {
      int heartRate = packet.getHeartRate();
      event = buildBioharnessEvent("Heart Rate", "HR", "beats per minute", "bpm", 40, 85, 130, 175, 220, heartRate);
      eventBus.post(event);
    }
    if(packet.isHeartRateVariabilityReliable())
    {
      int variability = packet.getHeartRateVariability();
      event = buildBioharnessEvent("Heart Rate Variability", "HRV", "milliseconds", "ms", 0, 70, 140, 210, 280, variability);
      eventBus.post(event);
    }
    if(packet.isECGReliable())
    {
      int ecgAmplitude = packet.getEcgAmplitude();
      int ecgNoise = packet.getEcgNoise();

      event = buildBioharnessEvent("ECG Amplitude", "ECGA", "microVolts", "uV", 0, 20000, 30000, 40000, 50000, ecgAmplitude);
      eventBus.post(event);
      event = buildBioharnessEvent("ECG Noise", "ECGN", "microVolts", "uV", 0, 20000, 30000, 40000, 50000, ecgNoise);
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

  SensorEvent buildBioharnessEvent(String longName,
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
