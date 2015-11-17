#include <SoftwareSerial.h>
#include <DHT.h>
#include <stdlib.h>

#define DHTPIN 9
#define DHTTYPE DHT22   // DHT 22  (AM2302)

int humi, cel, fah, kelv; 
unsigned long analogcount;
double analogtotal;

String inData;
unsigned long duration;
unsigned long starttime = 0;
unsigned long sampletime_ms = 1000;
unsigned long analogvalue = 0;
double hppcf = 0;
double ugm3 = 0;

DHT dht(DHTPIN, DHTTYPE);
SoftwareSerial mySerial(10, 11); //RX, TX

#define LEDPIN 13
#define BT_BUF_LEN 10
char bt_index = 0;
char bt_buffer[BT_BUF_LEN];
int8_t bt_connected = -1; // 0 = not connected (but been connected), 1 == connected, -1 == never been connected

uint8_t put_chr_buf(char c){
  // puts a character into the bt_buffer.
  // If the character is '\n' returns 1 (and doesn't put it into the buffer)
  // else returns 0
  if(c == '\n'){
      return 1;
  }
  if(bt_index >= BT_BUF_LEN - 2){
    return 0;
  }
  bt_buffer[bt_index] = c;
  bt_index++;
  bt_buffer[bt_index] = 0;
  return 0;
}

void analyze_buf(){
  // Determines if the buffer contains the SS notation
  if(bt_connected == -1){
    if(strncmp(bt_buffer, "SS=", 3) == 0){
      bt_connected = 0;
      Serial.println(F("1st Connect to BT"));
    }
  }
  else{
    if(strncmp(bt_buffer, "OK", 2) == 0){
    }
    else{
      Serial.println(F("ERROR: Unknown response:"));
      Serial.println(bt_buffer);
    }
    bt_connected = 0;
  }
  bt_buffer[0] = 0;
  bt_index = 0;
}

void update_bt_status(){
  // This should be called in the loop with at least a hundred milliescond delay before it is called again.
  // Determine if the BT module is connected or not.
  uint8_t temp = 0;
  if(mySerial.available()){
    while(mySerial.available()){
      temp = put_chr_buf(mySerial.read());
      if(temp){
        analyze_buf();
      }
    }
  }
  else{
    if(bt_connected >= 0){
      bt_connected = 1;
    }
  }
  
  // clear excess
  while(mySerial.available()){
    mySerial.read();
  }
  
  // Send out another signal for the next loop
  mySerial.println(F("BC:UI=01"));
  
  // Indicate connection status via the LED
  digitalWrite(LEDPIN, HIGH);
  if(bt_connected < 1){
    delay(100);
    digitalWrite(LEDPIN, LOW);
    delay(100);
  }
}

String readBTMAC() {
inData = "";
if (mySerial.available() > 0) {
Serial.println(mySerial.available());
int h = mySerial.available();
for (int i = 0; i < h; i++) {
inData += (char)mySerial.read();
}
inData.remove(23 ,24);
inData.remove(0, 11);
}
return inData;
}

void clear_bt_serial(){
  while(mySerial.available()){
    Serial.write(mySerial.read());
  }
}

void setup() {
  delay(3000);
  Serial.begin(115200);
  mySerial.begin(115200);
  mySerial.println("BC:BR=08");
  delay(100);
  
  mySerial.begin(19200);
  mySerial.println("BC:BR=08");
  delay(100);
  
  mySerial.begin(9600);
  mySerial.println("BC:BR=08");
  delay(100);
  
  mySerial.begin(19200);
  
  mySerial.println("BC:FT=00,00,00,01,03,0000");  // It will never try to autoconnect and will always be in discoverable mode.
  delay(100);
  mySerial.println(("BC:AD"));
  delay(100);
  readBTMAC();
  
  mySerial.print(("BC:NM=AirBeam-"));
  mySerial.println(inData);
  bt_buffer[0] = 0;
  pinMode(LEDPIN, OUTPUT);
  starttime = millis();
}

void loop() {
  analogvalue = analogvalue + analogRead(0);
  analogcount++;

  humi = dht.readHumidity();
  cel = dht.readTemperature();
  fah = ((cel * 9)/5) + 32;
 
  update_bt_status();
  if ((millis()-starttime) > sampletime_ms)
  {
    starttime = millis(); // moved to create more regular periods.
    analogvalue = analogvalue/analogcount;
    analogtotal = (analogvalue*5.0)/1024;
    hppcf = (240.0*pow(analogtotal,6) - 2491.3*pow(analogtotal,5) + 9448.7*pow(analogtotal,4) - 14840.0*pow(analogtotal,3) + 10684.0*pow(analogtotal,2) + 2211.8*(analogtotal) + 7.9623);
    ugm3 = .518 + .00274 * hppcf;
    if(ugm3 < 0){
      ugm3 = 0;
    }
     
    Serial.println();
    Serial.print("AirBeam MAC: ");
    Serial.print(inData);
    Serial.print(" ");
    Serial.print(fah);
    Serial.print(F("F "));
    Serial.print(cel);
    Serial.print(F("C "));
    Serial.print(humi);
    Serial.print(F("RH "));
    Serial.print(F(" Analog Total: "));
    Serial.print(analogtotal);
    Serial.print(F(" hppcf: "));
    Serial.print(hppcf);
    Serial.print(F(" ugm^3: "));
    Serial.print(ugm3);
    Serial.println();
    
    analogvalue = 0;
    analogcount = 0;
    //lowpulseoccupancy = 0;
    
    if(bt_connected > 0){
      //mySerial.print(analogtotal);
      //mySerial.print((";AirBeam:"));
      //mySerial.print(inData);
      //mySerial.print((";AirBeam-PM-V;Particulate Matter;PM;Volts;V;0;1;2;3;4"));
      //mySerial.print("\n");
      
      //mySerial.print(hppcf);
      //mySerial.print((";AirBeam:"));
      //mySerial.print(inData);
      //mySerial.print((";AirBeam-PM-hppcf;Particulate Matter;PM;hundreds of particles per cubic foot;hppcf;0;3000;8000;13000;26000"));
      //mySerial.print("\n");
      
      mySerial.print(ugm3);
      mySerial.print((";AirBeam:"));
      mySerial.print(inData);
      mySerial.print((";AirBeam-PM;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;12;35;55;150"));
      mySerial.print("\n");
      
      mySerial.print(fah);
      mySerial.print((";AirBeam:"));
      mySerial.print(inData);
      mySerial.print((";AirBeam-F;Temperature;F;degrees Fahrenheit;F;0;25;50;75;100"));
      mySerial.print("\n");
   
      //mySerial.print(cel);
      //mySerial.print((";AirBeam:"));
      //mySerial.print(inData);
      //mySerial.print((";AirBeam-C;Temperature;C;degrees Celsius;C;0;10;20;30;40"));
      //mySerial.print("\n");
      
      mySerial.print(humi);
      mySerial.print((";AirBeam:"));
      mySerial.print(inData);
      mySerial.print((";AirBeam-RH;Humidity;RH;percent;%;0;25;50;75;100"));
      mySerial.print("\n");
      
    }
    }
}
