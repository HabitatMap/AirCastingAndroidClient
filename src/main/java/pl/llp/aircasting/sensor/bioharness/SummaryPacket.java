package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.util.Constants;

public class SummaryPacket extends Packet
{
  private final int heartRate;
  private final int heartRateVariability;
  private final int galvanicSkinResponse;
  private final double respirationRate;
  private final double skinTemperature;
  private final double coreTemperature;

  private final double activity;

  private final boolean activityReliable;
  private final boolean heartRateReliable;
  private final boolean heartRateVariablityReliable;
  private final boolean skinTemperatureReliable;
  private final boolean respirationRateReliable;
  private final boolean coreTemperatureReliable;

  private final int breathingWaveAmplitude;
  private final int breathingWaveNoise;
  private final int ecgAmplitude;
  private final int ecgNoise;

  private final int verticalAccelerationMax;
  private final int lateralAccelerationMax;
  private final int sagittalAccelerationMax;
  private final int verticalAccelerationMin;
  private final int lateralAccelerationMin;
  private final int sagittalAccelerationMin;
  private final int peakAcceleration;
  private final long timeStamp;

  public SummaryPacket(byte[] input, int offset)
  {
    byte dlc = input[offset + 2];
    if(input.length - dlc < 0)
    {
      throw new RuntimeException("Not long enough");
    }

    Builder builder = new Builder(input, offset);

    this.timeStamp = builder.fromBytes().fourth(11).third(10).second(9).first(8).value();
    this.heartRate = builder.fromBytes().second(14).first(13).value();
    this.respirationRate = builder.fromBytes().second(16).first(15).value() / 10.0d;
    this.skinTemperature = (builder.fromBytes().second(18).first(17).value()) / 10.0d;
    this.heartRateVariability = (builder.fromBytes().second(39).first(38).value());
    this.coreTemperature = (builder.fromBytes().second(65).first(64).value()) / 10.0d;
    this.galvanicSkinResponse = (builder.fromBytes().second(42).first(41).value());
    this.activity = (builder.fromBytes().second(22).first(21).value());

    this.peakAcceleration = (builder.signedFromTwoBytes().second(24).first(23).value());
    this.verticalAccelerationMax = (builder.signedFromTwoBytes().second(48).first(47).value());
    this.lateralAccelerationMax = (builder.signedFromTwoBytes().second(52).first(51).value());
    this.sagittalAccelerationMax = (builder.signedFromTwoBytes().second(56).first(55).value());
    this.verticalAccelerationMin = (builder.signedFromTwoBytes().second(46).first(45).value());
    this.lateralAccelerationMin = (builder.signedFromTwoBytes().second(50).first(39).value());
    this.sagittalAccelerationMin = (builder.signedFromTwoBytes().second(54).first(53).value());

    this.breathingWaveAmplitude = builder.fromBytes().second(29).first(28).value();
    this.breathingWaveNoise = builder.fromBytes().second(31).first(30).value();
    this.ecgAmplitude = builder.fromBytes().second(34).first(33).value();
    this.ecgNoise = builder.fromBytes().second(36).first(35).value();

    byte ls = input[offset + 59];
    byte ms = input[offset + 60];

    boolean hruf =  (ls & 1 << 4) < 1;
    boolean rruf =  (ls & 1 << 5) < 1;
    boolean stuf =  (ls & 1 << 6) < 1;
    boolean acuf =  (ms & 1 << 0) < 1;
    boolean hrvuf = (ms & 1 << 1) < 1;
    boolean ectuf = (ms & 1 << 2) < 1;

    this.activityReliable = acuf;
    this.heartRateReliable = hruf;
    this.heartRateVariablityReliable = hrvuf;
    this.skinTemperatureReliable = stuf;
    this.respirationRateReliable = rruf;
    this.coreTemperatureReliable = ectuf;
    if(Constants.isDevMode())
    {
      String msg = String.format("" +
                                     "Activity: %b\n" +
                                     "HR: %b\n" +
                                     "HRA: %b\n" +
                                     "Skin temp: %b\n" +
                                     "Breathing rate: %b\n" +
                                     "core temp: %b",
                                 activityReliable,
                                 heartRateReliable,
                                 heartRateVariablityReliable,
                                 skinTemperatureReliable,
                                 respirationRateReliable,
                                 coreTemperatureReliable);
      Logger.d(msg);
    }
  }

  public int getHeartRate()
  {
    return heartRate;
  }

  public double getRespirationRate()
  {
    return respirationRate;
  }

  public double getSkinTemperature()
  {
    return skinTemperature;
  }

  public int getHeartRateVariability()
  {
    return heartRateVariability;
  }

  public int getGalvanicSkinResponse()
  {
    return galvanicSkinResponse;
  }

  public double getCoreTemperature()
  {
    return coreTemperature;
  }

  public boolean isRespirationRateReliable()
  {
    return respirationRateReliable && inRange(respirationRate, 0,  70);
  }

  public boolean isSkinTemperatureReliable()
  {
    return skinTemperatureReliable && inRange(skinTemperature, 0, 60);
  }

  public boolean isHeartRateVariabilityReliable()
  {
    return heartRateVariablityReliable && inRange(heartRateVariability, 0, 30000);
  }

  public boolean isHeartRateReliable()
  {
    return heartRateReliable && inRange(heartRate, 0, 240);
  }

  private boolean inRange(double value, int lower, int upper)
  {
    return value <= upper && value >= lower;
  }

  public boolean isGSRReliable()
  {
    return inRange(galvanicSkinResponse, 0, 65534);
  }

  public boolean isCoreTemperatureReliable()
  {
    return coreTemperatureReliable && inRange(coreTemperature, 32, 42);
  }

  public boolean isECGReliable()
  {
    return inRange(ecgAmplitude, 0, 50000);
  }

  public int getBreathingWaveAmplitude()
  {
    return breathingWaveAmplitude;
  }

  public int getBreathingWaveNoise()
  {
    return breathingWaveNoise;
  }

  public int getEcgAmplitude()
  {
    return ecgAmplitude;
  }

  public int getEcgNoise()
  {
    return ecgNoise;
  }

  public double getActivity()
  {
    return activity;
  }

  public boolean isActivityReliable()
  {
    return activityReliable && inRange(activity, 0, 1600);
  }

  public int getVerticalAccelerationMax()
  {
    return verticalAccelerationMax;
  }

  public int getLateralAccelerationMax()
  {
    return lateralAccelerationMax;
  }

  public int getSagittalAccelerationMax()
  {
    return sagittalAccelerationMax;
  }

  public int getVerticalAccelerationMin()
  {
    return verticalAccelerationMin;
  }

  public int getLateralAccelerationMin()
  {
    return lateralAccelerationMin;
  }

  public int getSagittalAccelerationMin()
  {
    return sagittalAccelerationMin;
  }

  public int getPeakAcceleration()
  {
    return peakAcceleration;
  }

  public long getTimeStamp()
  {
    return timeStamp;
  }
}
