package pl.llp.aircasting.sensor.bioharness;

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

  private final int breathingWaveAmplitude;
  private final int breathingWaveNoise;
  private final int ecgAmplitude;
  private final int ecgNoise;
  ;


  public SummaryPacket(byte[] input, int offset)
  {
    byte dlc = input[offset + 2];
    if(input.length - dlc < 0)
    {
      throw new RuntimeException("Not long enough");
    }

    Builder builder = new Builder(input, offset);

    this.heartRate = builder.intFromBytes().higher(14).lower(13).value();
    this.respirationRate = builder.intFromBytes().higher(16).lower(15).value() / 10.0d;
    this.skinTemperature = (builder.intFromBytes().higher(18).lower(17).value()) / 10.0d;
    this.heartRateVariability = (builder.intFromBytes().higher(39).lower(38).value());
    this.coreTemperature = (builder.intFromBytes().higher(65).lower(64).value()) / 10.0d;
    this.galvanicSkinResponse = (builder.intFromBytes().higher(42).lower(41).value());
    this.activity = (builder.intFromBytes().higher(22).lower(21).value()) / 100.0d;

    this.breathingWaveAmplitude = builder.intFromBytes().higher(29).lower(28).value();
    this.breathingWaveNoise = builder.intFromBytes().higher(31).lower(30).value();
    this.ecgAmplitude = builder.intFromBytes().higher(34).lower(33).value();
    this.ecgNoise = builder.intFromBytes().higher(36).lower(35).value();

    byte ls = input[offset + 59];
    byte ms = input[offset + 60];

    boolean hruf =  (ls & 1 << 4) < 1;
    boolean rruf =  (ls & 1 << 5) < 1;
    boolean stuf =  (ls & 1 << 6) < 1;
    boolean acuf =  (ms & 1 << 0) < 1;
    boolean hrvuf = (ms & 1 << 1) < 1;

    this.activityReliable = acuf;
    this.heartRateReliable = hruf;
    this.heartRateVariablityReliable = hrvuf;
    this.skinTemperatureReliable = stuf;
    this.respirationRateReliable = rruf;
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
    return inRange(coreTemperature, 32, 42);
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
    return activityReliable && inRange(activity, 0, 16);
  }
}
