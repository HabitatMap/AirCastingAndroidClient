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

#include <MeetAndroid.h>

float val, val0, val1 ,val2, humi, CO, kelv, cel,
fah, maxv;
int circ = 5, heat = 6;
MeetAndroid meetAndroid;

void setup()
{
  Serial.begin(115200);
  pinMode(circ, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(11, OUTPUT);
}

void loop()
{
  meetAndroid.receive();

  //call up the calculation functions
  GetHumi();
  GetTemp();
  GetCO();
  //Display of humidity
  Serial.print(humi);
  Serial.print(";CityTech56789;HIH4030;Humidity;RH;percent;%;0;25;50;75;100");
  Serial.print("\n");
  //Display of CO gas sensor
  Serial.print(CO);
  Serial.print(";CityTech56789;TGS2442;CO Gas;CO;parts per million;ppm;0;10;20;30;40");
  Serial.print("\n");
  //Serial.print("% ");
  //Display of temperature in K, C, and F
  Serial.print(kelv);
  Serial.print(";CityTech56789;LM335A-K;Temperature;K;kelvin;K;255;270;283;297;310");
  Serial.print("\n");
  Serial.print(cel);
  Serial.print(";CityTech56789;LM335A-C;Temperature;C;degrees Celsius;C;-20;-5;10;25;40");
  Serial.print("\n");
  Serial.print(fah);
  Serial.print(";CityTech56789;LM335A-F;Temperature;F;degrees Fahrenheit;F;0;25;50;75;100");
  Serial.print("\n");
}

void GetTemp()
{
  val2 = analogRead(A2);
  kelv = val2 * 0.48875855;
  cel = kelv - 273.15;
  fah = (kelv * 1.8) - 459.67;
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
  val1 = analogRead(A1);
  CO = map(val1, 0 , 1023, 0, 100);
  if(CO < 20 && CO >= 0)
  {
    digitalWrite(9, HIGH);
    digitalWrite(10, LOW);
    digitalWrite(11, LOW);
  }
  if(CO < 30 && CO >= 20)
  {
    digitalWrite(9, LOW);
    digitalWrite(10, HIGH);
    digitalWrite(11, LOW);
  }
  if(CO >= 30)
  {
    digitalWrite(9, LOW);
    digitalWrite(10, LOW);
    digitalWrite(11, HIGH);
  }
  delay(2);
}

void GetHumi()
{
  val0 = analogRead(A0);
  maxv = (3.27-(0.006706*cel));
  humi = ((((val0/1023)*5)-0.8)/maxv)*100;
}


