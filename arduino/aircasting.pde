
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

float val, val0, val1, val2, val3, maxv, CO, NO2; //Decimal value variables
int humi, kelv, cel, fah, circ = 5, heat = 6; //Integer value variables

void setup()
{
  Serial.begin(115200); //Serial communication for Arduino Serial Monitor
  mySerial.begin(115200); //Serial communcation for Aircasting Application
  pinMode(circ, OUTPUT);
  pinMode(heat, OUTPUT);
}

//Changes map function from interger values to decimal values
float map(float x, float in_min, float in_max, float out_min, float out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

//Get NO2 gas function
void GetNO2(){
  val3 = analogRead(A3);
  NO2 = map(val3, 1023, 0, 0, 100);
}

//Get temperature in Kelvins, Celsius and Fahrenhiet function
void GetTemp()
{
  val2 = analogRead(A2);
  val2 = (val2 * 500)/1023;
  kelv = val2;
  cel = val2 - 273.15;
  fah = ((val2 * 9)/5.0) - 459.67;
}

//Get CO gas function
void GetCO()
{
  digitalWrite(circ, HIGH);
  delay(3);
  val1 = analogRead(A1);
  CO = map(val1, 0 , 1023, 0, 100);
  delay(2);
  digitalWrite(circ, LOW);
  analogWrite(heat, 245);
  delay(14);
  analogWrite(heat, 0);
  delay(981);
  
}

//Get humidity function
void GetHumi()
{
  val0 = analogRead(A0);
  maxv = (3.27-(0.006706*cel));
  humi = ((((val0/1023)*5)-0.8)/maxv)*100;
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
  mySerial.print(";CityTech56789;HIH4030;Humidity;RH;percent;%;0;25;50;75;100");
  mySerial.print("\n");
  //Display of CO gas sensor
  mySerial.print(CO);
  mySerial.print(";CityTech56789;TGS2442;CO Gas;CO;parts per million;ppm;0;25;50;75;100");
  mySerial.print("\n");
  //Serial.print("% ");
  //Display of temperature in K, C, and F
  mySerial.print(kelv);
  mySerial.print(";CityTech56789;LM335A-K;Temperature;K;kelvin;K;273;300;400;500;600");
  mySerial.print("\n");
  mySerial.print(cel);
  mySerial.print(";CityTech56789;LM335A-C;Temperature;C;degrees Celsius;C;0;10;15;20;25");
  mySerial.print("\n");
  mySerial.print(fah);
  mySerial.print(";CityTech56789;LM335A-F;Temperature;F;degrees Fahrenheit;F;0;25;50;75;100");
  mySerial.print("\n");
  
  mySerial.print(NO2);
  mySerial.print(";CityTech56789;MiCS-2710;N02 Gas;NO2;parts per million;ppm;0;25;50;75;100");
  mySerial.print("\n");

  //Display values for Arduino serial monitor 
  Serial.print("Temperature: ");
  Serial.print(kelv);
  Serial.print("K ");
  Serial.print(cel);
  Serial.print("C ");
  Serial.print(fah);
  Serial.print("F ");
  
  Serial.print("Humidity: ");
  Serial.print(humi);
  Serial.print("% ");
  
  Serial.print("CO Gas: ");
  Serial.print(CO);
  Serial.print("% ");
  
  Serial.print("NO2 Gas: ");
  Serial.print(NO2);
  Serial.println("%");
  
}