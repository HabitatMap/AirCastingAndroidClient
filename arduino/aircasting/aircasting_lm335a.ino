/*
Each sensor reading should be written as one line to the serial output. Lines should end
with '\n' and should have the following format:

<Measurement value>;<Sensor package name>;<Sensor name>;<Type of measurement>;<Short type of measurement>;<Unit name>;<Unit symbol/abbreviation>;<T1>;<T2>;<T3>;<T4>;<T5>

The Sensor name should be different for each sensor.

T1..T5 are integer thresholds which guide how values should be displayed -
- lower than T1 - extremely low / won't be displayed
- between T1 and T2 - low / green
- between T2 and T3 - medium / yellow
- between T3 and T4 - high / orange
- between T4 and T5 - very high / red
- higher than T5 - extremely high / won't be displayed
*/

#include <SoftwareSerial.h> //Header for software serial communication
SoftwareSerial mySerial(2, 3); //Assign 2 as Rx and 3 as Tx

float maxv, CO, NO2;
int humi, kelv, cel, fah, circ = 5, heat = 6;

void setup()
{
  Serial.begin(115200); //Serial communication for Arduino Serial Monitor
  mySerial.begin(115200); //Serial communcation for Aircasting Application
  pinMode(circ, OUTPUT);
  pinMode(heat, OUTPUT);
}

void GetTemp()
{
  float val2 = analogRead(A2);
  kelv = val2 * 0.48875855;
  cel = kelv - 273.15;
  fah = (kelv * 1.8) - 459.67;
}

void loop()
{
  //call up the calculation functions
  GetHumi();
  GetTemp();
  GetCO();
  GetNO2();

  //Display of humidity
  mySerial.print(humi);
  mySerial.print(";InsertSensorPackageName;HIH4030;Humidity;RH;percent;%;0;25;50;75;100");
  mySerial.print("\n");
  Serial.print("Humidity: ");
  Serial.print(humi);
  Serial.print("% ");

  //Display of CO gas sensor
  mySerial.print(CO);
  mySerial.print(";InsertSensorPackageName;TGS2442;CO Gas;CO;response indicator;RI;0;25;50;75;100");
  mySerial.print("\n");
  Serial.print("CO Gas: ");
  Serial.print(CO);
  Serial.print("% ");

  //Display of temperature in K, C, and F
  /*
  mySerial.print(kelv);
  mySerial.print(";InsertSensorPackageName;LM335A;Temperature;K;kelvin;K;255;270;283;297;310");
  mySerial.print("\n");
  mySerial.print(cel);
  mySerial.print(";InsertSensorPackageName;LM335A;Temperature;C;degrees Celsius;C;-20;-5;10;25;40");
  mySerial.print("\n");
  */
  mySerial.print(fah);
  mySerial.print(";InsertSensorPackageName;LM335A;Temperature;F;degrees Fahrenheit;F;0;30;60;90;120");
  mySerial.print("\n");

  Serial.print("Temperature: ");
  Serial.print(kelv);
  Serial.print("K ");
  Serial.print(cel);
  Serial.print("C ");
  Serial.print(fah);
  Serial.print("F ");

  mySerial.print(NO2);
  mySerial.print(";InsertSensorPackageName;MiCS-2710;N02 Gas;NO2;response indicator;RI;0;25;50;75;100");
  mySerial.print("\n");
  Serial.print("NO2 Gas: ");
  Serial.print(NO2);
  Serial.println("%");
}

void GetCO()
{
  digitalWrite(circ, LOW);
  analogWrite(heat, 245);
  delay(14);
  analogWrite(heat, 0);
  delay(981);
  digitalWrite(circ, HIGH);
  delay(3);
  float val1 = analogRead(A1);
  CO = map(val1, 0 , 1023, 0, 100);
}

void GetHumi()
{
  float val0 = analogRead(A0);
  float maxv = (3.27-(0.006706*cel));
  humi = ((((val0/1023)*5)-0.8)/maxv)*100;
}

void GetNO2()
{
  float val3 = analogRead(A3);
  NO2 = map(val3, 1023, 0, 0, 100);
}

float map(float x, float in_min, float in_max, float out_min, float out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}