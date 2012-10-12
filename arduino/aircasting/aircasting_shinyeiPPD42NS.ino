//I. Heng and R. Yap
#include <SoftwareSerial.h>
unsigned long duration;
unsigned long starttime;
unsigned long sampletime_ms = 1000;
unsigned long lowpulseoccupancy = 0;
double ratio = 0;
double concentration = 0;

SoftwareSerial mySerial(2, 3);

void setup() {
  Serial.begin(9600);
  mySerial.begin(115200);
  pinMode(8,INPUT);
  starttime = millis();
}

void loop() {
  duration = pulseIn(8, LOW);
  lowpulseoccupancy = lowpulseoccupancy+duration;
  //Serial.print(starttime);
  //Serial.print(" ");
  //Serial.println(duration);
  if ((millis()-starttime) > sampletime_ms)
  {
    ratio = lowpulseoccupancy/(sampletime_ms*10.0);  // Integer percentage 0=>100
    concentration = (1.1*pow(ratio,3)-3.8*pow(ratio,2)+520*ratio+0.62)/100; // using spec sheet curve
    Serial.print(lowpulseoccupancy);
    Serial.print(",");
    Serial.print(ratio);
    Serial.print(",");
    Serial.println(concentration);
    mySerial.print(concentration);
    mySerial.print(";InsertSensorPackageName;PPD42NS;Particulate Matter;PM;hundred particles > 1 um per 0.01 cubic feet;hppcf;0;100;200;300;400");
    mySerial.print("\n");
    lowpulseoccupancy = 0;
    starttime = millis();
  }
}