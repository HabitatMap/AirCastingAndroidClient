#include <SoftwareSerial.h>
#include <TimeLib.h>
#include <EEPROM.h>
#include "Strings.h"
#include "Time_Sync.h"
#include "Color.h"
#include "PMS.h"
#include "Build_String.h"
#include "Read_EEPROM.h"

#define pt_Serial Serial1

#define BT_BUF_LEN 10 //BT buffer size 
#define ARRAY_SIZE 1  //size of buffer to hold first hex value
#define PACKET_BUF_LEN 50 //size of buffer for hex parameters

char buff[200]; //temporary buffer for PROGMEM
char conf [50]; //temporary buffer for saved configuration
int fah, real_hum;  //Variables used for sensors
double cel;
//Strings used for parameters set via BT//
String ssid = "";
String pass = "";
String uuid = "";
String uuidauth = "";
String longitude = "";
String latitude = "";
//String sensor_package = "";
String zone = "";

String inData; //MAC address
String post_data = ""; //Used to build HTTP strings
String Aircastingserver = "www.aircasting.org";//Host name

int Zone; //Use for conversion String -> Int
int trigger = 0;  // Triggers the PMS readings
int flag = -1;  //Used to set time sync once
int mode = -1;  //Used to select streaming method
int done = -1;
int stream_light = 1;
uint8_t i = 0;

boolean config_window = false;
//boolean led_timeout = false; 
boolean bt_disconnected = true; 
boolean commLED = false;

volatile int bt_need_update = 0; // These two need to be volatile since they can change inside interrupts
volatile int pm_need_update = 0;
volatile int setup_timer = 0;

int mode_set_eeprom;

int x;  // Variable to check PMS

/*Time variables*/

int years;
int months;
int days;
int hours;
int mins;
int secs;

int avgyears;
int avgmonths;
int avgdays;
int avghours;
int avgmins;
int avgsecs;

char replybuffer_[17];  

#define btm_reset 30
#define wifi_reset 29
#define pt_reset 35
#define dtr_cell 21
#define cell_reset 0

/*PMS variables*/
//int inputHigh = 0;
//int inputLow = 0;

/*BT updates*/
uint8_t incomingByte = 0;
uint8_t firstByte [ARRAY_SIZE];
uint8_t packet_index = 0;
uint8_t packet_buffer[PACKET_BUF_LEN];

//unsigned long currentMillis = 0;
unsigned long starttime0 = 0;  //Used for PM Streaming
unsigned long starttime1 = 0; //Used for Streaming
unsigned long starttime2 = 0; //Used for Comm LEDs
unsigned long timer_expired = 60000;
unsigned long currentMillis = 0;
unsigned long led_stream = 0;
//unsigned long LED_on = 0;
//unsigned long LED_off = 0;
//unsigned long timeNow = 0;
/*Analog readings*/
//uint16_t analog_cel;
//uint16_t analog_hum;
//unsigned long analogcount;

/*BT Updates*/
char bt_index = 0;
char bt_buffer[BT_BUF_LEN];

/*PMS global variables*/
unsigned int concPM1_0_CF1 = 0;
unsigned int concPM2_5_CF1 = 0;
unsigned int concPM10_0_CF1 = 0;
unsigned int concPM1_0_amb = 0;
unsigned int concPM2_5_amb = 0;
unsigned int concPM10_0_amb = 0;
unsigned int rawGt0_3um = 0;
unsigned int rawGt0_5um = 0;
unsigned int rawGt1_0um = 0;
unsigned int rawGt2_5um = 0;
unsigned int rawGt5_0um = 0;
unsigned int rawGt10_0um = 0;
unsigned int BAMPM10;
unsigned int BAMPM2_5;
unsigned int GRIMMPM10;
unsigned int GRIMMPM2_5;
unsigned int GRIMMPM1;

unsigned int PM1_0;
unsigned int PM2_5;
unsigned int PM10_0;

/*Software Serials*/
SoftwareSerial btm_Serial(25, 31);
SoftwareSerial wifi_Serial(27, 13);
SoftwareSerial cellular_Serial(23, 1);

uint8_t put_chr_buf(char c) {
  if (c == '\n') {
    return 1;
  }
  if (bt_index >= BT_BUF_LEN - 2) {
    return 0;
  }

  bt_buffer[bt_index] = c;
  bt_index++;
  bt_buffer[bt_index] = 0;
  return 0;
}

/*Fill buffer with HEX values sent via BT*/
uint8_t put_hex_buf(uint8_t x) {
  if (x == 0xFF) {
    return 1;
  }
  packet_buffer[packet_index] = x;
  packet_index++;
  packet_buffer[packet_index] = 0;
  return 0;
}

/*Analyze buffers*/
void analyze_buf() {
  /*Show BT disconnected status*/
  if (strncmp(bt_buffer, "SS=00", 5) == 0 || strncmp(bt_buffer, "SS=10", 5) == 0 || strncmp(bt_buffer, "SS=20", 5) == 0 || strncmp(bt_buffer, "SS=30", 5) == 0 || strncmp(bt_buffer, "SS=40", 5) == 0 || strncmp(bt_buffer, "SS=50", 5) == 0) {
    //mode = -1;
    bt_disconnected = true;
    Serial.println(F("Bluetooth Disconnected"));
  }

  /*Show BT connected status*/
  if (strncmp(bt_buffer, "SS=02", 5) == 0 || strncmp(bt_buffer, "SS=12", 5) == 0 || strncmp(bt_buffer, "SS=22", 5) == 0 || strncmp(bt_buffer, "SS=32", 5) == 0 || strncmp(bt_buffer, "SS=42", 5) == 0 || strncmp(bt_buffer, "SS=52", 5) == 0) {
    bt_disconnected = false;
    Serial.println(F("Bluetooth Connected"));
  }

  /*Disconnect from BT and set sleep mode if WiFi or Cellular is selected*/
  if (strncmp(bt_buffer, "OK", 2) == 0 && flag == 1) {
    flag = 0;
    btm_Serial.println(F("BC:DC"));  //Diconnect from BT
    btm_Serial.println(F("BC:SP=01"));  //sleep
    Serial.println(F("Bluetooth Asleep"));
  }

  /*BT is selected as streaming method*/
  if (packet_buffer[0] == 0x01) {  
    EEPROM.write(0, 1);   
    Serial.println(F("Bluetooth Selected"));
    btm_Serial.println("BC:SP=00");  //sleep enabled 
    trigger = 1;  //Start PMS readings
    mode = 0; //Set streaming method as BT 
    flag = 0;
    done = 0;
    //switch_timer();
  }

  /*WiFi is selected as streaming method*/
  if (packet_buffer [0] == 0x02) {
    Serial.println(F("WiFi Selected"));
    delay(1000);
    btm_Serial.println("BC:BP=00,00");  //Allow commands when connected
    delay(1000);

    /*Get parameters from hex code with SSID, PASS, and zone*/
    String str = (char*)packet_buffer;
    int commaIndex = str.indexOf(',');
    int secondcommaIndex = str.indexOf(',', commaIndex + 1);
    ssid = str.substring(1, commaIndex);
    pass = str.substring(commaIndex + 1, secondcommaIndex);
    zone = str.substring(secondcommaIndex + 1);
    Zone = zone.toInt();
    //Serial.println(ssid);
    //Serial.println(pass);
    //Serial.println(Zone);

    //Write variables in EEPROM//    
    int len = ssid.length();
    EEPROM.write(5, len);    
    int len_1 = pass.length();
    EEPROM.write(6, len_1);    
    int len_2 = zone.length();
    EEPROM.write(7, len_2);    
    int offset = 8+EEPROM.read(1)+EEPROM.read(2)+ EEPROM.read(3)+EEPROM.read(4);    
    ssid.toCharArray(conf, len+1);
    for (int i=0; i <len+1; i++){
      EEPROM.write(offset + i, conf[i]);
    }    
    offset = 8+EEPROM.read(1)+EEPROM.read(2)+ EEPROM.read(3)+EEPROM.read(4)+EEPROM.read(5);
    pass.toCharArray(conf, len_1+1);
    for (int i=0; i <len_1+1; i++){
      EEPROM.write(offset + i, conf[i]);
    }  
    offset = 8+ EEPROM.read(1)+EEPROM.read(2)+ EEPROM.read(3)+EEPROM.read(4)+EEPROM.read(5)+EEPROM.read(6);     
    zone.toCharArray(conf, len_2+1);
    for (int i=0; i <len_2+1; i++){
      EEPROM.write(offset + i, conf[i]);
    }     
    EEPROM.write(0, 2);    
    mode = 1; //Set streaming method as WiFi
    flag = 1; //Flag set to start time sync
    trigger = 1; // Added the trigger = 1 and switch timer here
    //switch_timer();
  }
  if (packet_buffer[0] == 0x03){
    Serial.println(F("Cellular Selected"));
    delay(1000);
    btm_Serial.println("BC:BP=00,00");  //Allow commands when connected 
    delay(1000);    
    EEPROM.write(0, 3);
    mode = 2;
    flag = 1;
    trigger = 1; // Added the trigger = 1 and switch timer here
    //switch_timer();
  }

  /*Hex code with UUID*/
  if (packet_buffer[0] == 0x04) {
    config_window = true;
    String str = (char*)packet_buffer;
    uuid = str.substring(1);
    Serial.println(F("UUID:"));
    Serial.println(uuid);
    int len = uuid.length();
    EEPROM.write(1, len);    
    uuid.toCharArray(conf, len+1);
    for (int i=0; i<len+1; i++){
      EEPROM.write(8+i, conf[i]);
    } 
  }

  /*Hex code with uuidAuth*/
  if (packet_buffer[0] == 0x05) {
    Serial.println(F("UUIDAuth:"));
    String str = (char*)packet_buffer;
    uuidauth = str.substring(1);
    Serial.println(uuidauth);
    int len = uuidauth.length();
    EEPROM.write(2, len);    
    int offset = 8+EEPROM.read(1);    
    uuidauth.toCharArray(conf, len+1);
    for (int i=0; i<len+1; i++){
      EEPROM.write(offset + i, conf[i]);
    }
  }

  /*Hex code with latitude and longitude*/
  if (packet_buffer[0] == 0x06) {
    String str = (char*)packet_buffer;
    int commaIndex = str.indexOf(',');
    longitude = str.substring(1, commaIndex);
    latitude = str.substring(commaIndex + 1);
    Serial.println(F("Longitude:"));
    Serial.println(longitude);
    Serial.println(F("Latitude:"));
    Serial.println(latitude);   
    int len = longitude.length();
    EEPROM.write(3, len);    
    int len_1 = latitude.length();
    EEPROM.write(4, len_1);    
    int offset = 8+EEPROM.read(1)+EEPROM.read(2);    
    longitude.toCharArray(conf, len+1);
    for (int i=0; i <len+1; i++){
      EEPROM.write(offset+i, conf[i]);
    }    
    offset = 8+EEPROM.read(1)+EEPROM.read(2)+ EEPROM.read(3);    
    latitude.toCharArray(conf, len_1 + 1);
    for (int i=0; i <len_1+1; i++){
      EEPROM.write(offset+i, conf[i]);
    }  
  }

  /*Reset BT buffer stuff*/
  bt_buffer[0] = 0;
  bt_index = 0;
  packet_buffer[0] = 0;
  packet_index = 0;
}

void update_bt_status() {
  btm_Serial.listen();
  uint8_t temp = 0;
  uint8_t data1 = 0;
  if(btm_Serial.available()) {//if
    incomingByte = btm_Serial.read();
    firstByte[0] = incomingByte;
    if (incomingByte == 254) { //Hex code was received
      while (btm_Serial.available()) {//while
        data1 = put_hex_buf(btm_Serial.read());
        if (data1) {
          analyze_buf();
        }
      }

    }
    else { // BT reply
      bt_buffer[0] = firstByte[0];
      bt_index = 1;
      while (btm_Serial.available()) {//while
        temp = put_chr_buf(char(btm_Serial.read()));
        if (temp) {
          //Serial.println(bt_buffer);
          analyze_buf();
        }
      }
    }
  }
  //while(btm_Serial.read() != -1) {
  //}; //Clear serial
}

/*Get MAC address*/
String readBTMAC() {
  inData = "";
  if (btm_Serial.available() > 0) {
    int h = btm_Serial.available();
    for (int i = 0; i < h; i++) {
      inData += (char)btm_Serial.read();
    }
    inData.remove(23 , 24);
    inData.remove(0, 11);
  }
  return inData;
}

void reset_btm() {
  digitalWrite(btm_reset, LOW);
  delay(200);
  digitalWrite(btm_reset, HIGH);
}

void reset_cell() {
  digitalWrite(cell_reset, LOW);
  delay(200);
  digitalWrite(cell_reset, HIGH);
}

void reset_esp(){
  digitalWrite(wifi_reset, LOW);
  delay(200);
  digitalWrite(wifi_reset, HIGH);  
}

/*void OKprompter() {
 Serial.print(F("Searching for OK prompt."));
 i = 0;
 while(!wifi_Serial.find("OK")) // CHECK PM
 {
 Serial.print(F("."));
 i = i + 1;
 if (i > 5) {
 Serial.print(F("Error!"));
 break;
 }
 pms(); 
 }
 Serial.println(F("Done"));
 }
 
 void Arrowprompter() {
 Serial.print(F("Searching for > prompt."));
 int i = 0;
 while(!wifi_Serial.find(">"))  // CHECK PM
 {
 Serial.print(F("."));
 i = i + 1;
 if (i > 5) {
 Serial.print(F("Error!"));
 break;
 }
 pms(); 
 }
 Serial.println(F("Done"));
 }*/

void connect_AP(){
  uint8_t i  = 0;
  wifi_Serial.print("AT+CWJAP=\"" + ssid + "\",\"" + pass + "\"\r\n");
  while(!wifi_Serial.find("OK") &&  i ++ < 5) {   // CHECK PM
    pms(); 
  }
  if (i == 6){
    Serial.println(F("Could not connect to Wifi"));
  }
  else {
    Serial.println(F("Connected to Wifi")); 
  }   
}

void open_TCP_WiFi(){
  uint8_t i = 0;
  wifi_Serial.print("AT+CIPSTART=\"TCP\",\"" + Aircastingserver + "\",80\r\n"); //Establish connection with server
  while(!wifi_Serial.find("OK") && i++ < 5) {  // CHECK PM
    pms(); 
  }
  if (i == 6){
    Serial.println(F("Could Not Open WiFi TCP"));
  }

  else {
    Serial.println(F("WiFi TCP Opened"));
  }   
}

void send_OK_WiFi(){
  uint8_t i = 0;
  while(!wifi_Serial.find("OK") && i++ < 5) {  // CHECK PM
    pms(); 
  }
  if (i == 6){
    Serial.println("WiFi String Could Not Send");
  }
  else {
    Serial.println("WiFi String Sent");   
  } 
}

void close_TCP_WiFi(){
  uint8_t i = 0;
  wifi_Serial.print("AT+CIPCLOSE\r\n");
  while(!wifi_Serial.find("OK") && i++ <5) {  // CHECK PM
    pms(); 
  }
  if (i == 6){
    Serial.println(F("Could Not Close WiFi TCP "));
  }
  else {
    Serial.println(F("WiFi TCP Closed"));
  }
}

void quit_AP(){
  uint8_t i = 0;
  wifi_Serial.print("AT+CWJAP=\"" + ssid + "\",\"" + pass + "\"\r\n");
  while(!wifi_Serial.find("OK") && i++ <5) {  // CHECK PM
    pms(); 
  }
  if (i == 6){
    Serial.println(F("Could not Disconnect from WiFi"));
  }
  else{
    Serial.println(F("Disconnected from WiFi"));
  }
}

void open_TCP_Cell(){
  uint8_t i = 0;
  cellular_Serial.print("AT+CIPSTART=\"TCP\",\"" + Aircastingserver + "\",\"80\"\r\n"); //Establish connection with server
  while(!cellular_Serial.find("OK") && i ++ <5) {  // CHECK PM
    cellular_Serial.print("AT+CIPSTART=\"TCP\",\"" + Aircastingserver + "\",\"80\"\r\n");
    pms(); 
  }
  if (i == 6){
    Serial.println(F("Could Not Open Cellular TCP"));
  }
  else{
    Serial.println(F("Cellular TCP Opened"));
  }
}

void send_OK_Cell(){
  uint8_t i =0; 
  while(!cellular_Serial.find("OK") && i++ <5) {  // CHECK PM
    pms(); 
  }  
  if (i == 6){
    Serial.println(F("Cellular String Could Not Send"));
  }
  else{
    Serial.println(F("Cellular String Sent"));
  }
}

void close_TCP_Cell(){
  uint8_t i = 0;
  cellular_Serial.print("AT+CIPCLOSE\r\n");
  while(!cellular_Serial.find("OK") && i++ < 5) {  // CHECK PM
    cellular_Serial.print("AT+CIPCLOSE\r\n");
    pms(); 
  }
  if (i == 6){
    Serial.println(F("Could Not Close Cellular TCP"));
  }
  else{
    Serial.println(F("Cellular TCP Closed"));
  } 
}

void senddata(unsigned int data, byte caseswitch) {
  int len = 0;  //Stores data length
  int len_1 = 0;  //Stores total length
  getPost(); //Build Post string
  len_1 = post_data.length(); //Get post String length
  post_data = ""; //Clear string
  getData_1(caseswitch);
  len = len + post_data.length();
  post_data = "";
  getData_3(avgyears, avgmonths, avgdays, avghours, avgmins, avgsecs, data);
  len = len + post_data.length();
  post_data = "";
  getData_2(caseswitch);
  len = len + post_data.length();
  post_data = "";
  if (mode == 1){
    uint8_t i = 0;
    open_TCP_WiFi();
    wifi_Serial.print("AT+CIPSEND=" + String(len + len_1 + 20 + 3) + "\r\n"); //Set data length. 20->includes "\nConnection: close\n\n" length. 3->Includes "len" length
    while(!wifi_Serial.find(">") && i++ <5) {  // CHECK PM
      pms(); 
    }
    if (i == 6){
      Serial.println(F("\">\" Not Found"));
    }
    else {
      Serial.println(F("\">\" Found"));
    }

    /*Build Strings to send over WiFi*/
    getPost();
    wifi_Serial.print(post_data);
    post_data = "";
    wifi_Serial.print(len);
    wifi_Serial.print("\nConnection: close\n\n");  
    getData_1(caseswitch);
    wifi_Serial.print(post_data);
    post_data = "";
    getData_3(avgyears, avgmonths, avgdays, avghours, avgmins, avgsecs, data);
    wifi_Serial.print(post_data);
    post_data = "";  
    getData_2(caseswitch);
    wifi_Serial.print(post_data);
    post_data = "";
    wifi_Serial.print("\r\n");    
    send_OK_WiFi();
    close_TCP_WiFi();
  } 
  if (mode == 2){
    uint8_t i = 0;
    open_TCP_Cell();   
    cellular_Serial.print("AT+CIPSEND=" + String(len + len_1 + 20 + 3) + "\r\n"); //Set data length. 20->includes "\nConnection: close\n\n" length. 3->Includes "len" length
    while(!cellular_Serial.find(">") && i++ <5) {  // CHECK PM
      cellular_Serial.print("AT+CIPSEND=" + String(len + len_1 + 20 + 3) + "\r\n");
      pms(); 
    }  
    if (i == 6){
      Serial.println(F("\">\" Not Found"));
    }
    else {
      Serial.println(F("\">\" Found"));
    }

    getPost();
    cellular_Serial.print(post_data);
    post_data = "";
    cellular_Serial.print(len);
    cellular_Serial.print("\nConnection: close\n\n");  
    getData_1(caseswitch);
    cellular_Serial.print(post_data);
    post_data = "";  
    getData_3(avgyears, avgmonths, avgdays, avghours, avgmins, avgsecs, data);
    cellular_Serial.print(post_data);
    post_data = "";  
    getData_2(caseswitch);
    cellular_Serial.print(post_data);
    post_data = "";
    cellular_Serial.print("char(26)");    
    send_OK_Cell();
    close_TCP_Cell();
  }  
}

void set_cell_baud_rate(){
  cellular_Serial.listen();
  cellular_Serial.begin(115200);
  delay(200);
  cellular_Serial.print("AT+IPR=9600\r\n");  
  cellular_Serial.begin(57600);
  delay(200);
  cellular_Serial.print("AT+IPR=9600\r\n");  
  cellular_Serial.begin(38400);
  delay(200);
  cellular_Serial.print("AT+IPR=9600\r\n");
  cellular_Serial.begin(19200);
  delay(200);
  cellular_Serial.print("AT+IPR=9600\r\n");  
  cellular_Serial.begin(9600);
  delay(200);
}

void set_ESP_baud_rate(){
  wifi_Serial.listen();
  wifi_Serial.begin(115200);
  delay(200); 
  wifi_Serial.write("AT+UART=9600,8,1,0,0\r\n"); 
  wifi_Serial.begin(57600);
  delay(200);
  wifi_Serial.write("AT+UART=9600,8,1,0,0\r\n"); 
  wifi_Serial.begin(38400);
  delay(200);
  wifi_Serial.write("AT+UART=9600,8,1,0,0\r\n");
  wifi_Serial.begin(19200);
  delay(200);
  wifi_Serial.write("AT+UART=9600,8,1,0,0\r\n");  
  wifi_Serial.begin(9600);
  delay(200);
}

void set_btm_baud_rate(){
  btm_Serial.listen();
  btm_Serial.begin(115200);
  delay(200);
  btm_Serial.println(F("BC:BR=08"));   
  btm_Serial.begin(57600);
  delay(200);
  btm_Serial.println(F("BC:BR=08"));
  btm_Serial.begin(38400);
  delay(200);
  btm_Serial.println(F("BC:BR=08"));   
  btm_Serial.begin(19200);
  delay(200);
  btm_Serial.println(F("BC:BR=08"));   
  btm_Serial.begin(14400);
  delay(200);
  btm_Serial.println(F("BC:BR=08"));  
  btm_Serial.begin(9600);
  delay(200);
  btm_Serial.println(F("BC:BR=08"));
  btm_Serial.begin(19200);
  delay(200);
}

void SMS_sleep(){
  uint8_t i = 0;
  digitalWrite(dtr_cell, HIGH);  //Needs to be high to enter sleep mode
  cellular_Serial.begin(9600);
  delay(200);
  cellular_Serial.listen();
  cellular_Serial.print("AT+CSCLK=1\r\n");
  while(!cellular_Serial.find("OK") && i++ <5){
    cellular_Serial.print("AT+CSCLK=1\r\n");
  }
  if (i == 6){
    Serial.println(F("Cellular Could Not Sleep"));
  }
  else {
    Serial.println(F("Cellular Asleep"));
  }
  cellular_Serial.end();
}

void ESP_deep_sleep(){
  wifi_Serial.begin(9600);
  delay(200);
  wifi_Serial.listen();
  wifi_Serial.print("AT+GSLP=0\r\n");
  //OKprompter();
  /*wifi_Serial.print("AT+SLEEP=1\r\n");
   while(!wifi_Serial.find("OK")){
   wifi_Serial.print("AT+SLEEP=1\r\n");
   }*/
  Serial.println(F("WiFi Asleep"));
  wifi_Serial.end();
}

// Took these out of the loop and made them into functions. Loop calls them when needed.
// Function to Stream Bluetooth Data

void stream_bt_data(){  
  starttime1 = millis();
  if(done == 0){
    done = 1;     
    ESP_deep_sleep();  
    SMS_sleep();
  }      
  btm_Serial.listen();
  btm_Serial.print(fah);
  btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-F;Temperature;F;degrees Fahrenheit;F;15;45;75;100;135");
  btm_Serial.print(real_hum);
  btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-RH;Humidity;RH;percent;%;0;25;50;75;100");
  btm_Serial.print(PM1_0);
  btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM1;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;12;35;55;150");
  btm_Serial.print(PM2_5);
  btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM2.5;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;12;35;55;150");
  btm_Serial.print(PM10_0);
  btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM10;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;20;50;100;200");

  /*
   btm_Serial.print(rawGt0_3um); 
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-Count 0.3;Particulate Matter;PM;particles per .1 liter;ppl;0;1000;2000;3000;4000");
   btm_Serial.print(rawGt0_5um);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-Count 0.5;Particulate Matter;PM;particles per .1 liter;ppl;0;1000;2000;3000;4000");
   btm_Serial.print(rawGt1_0um); 
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-Count 1;Particulate Matter;PM;particles per .1 liter;ppl;0;1000;2000;3000;4000");
   btm_Serial.print(rawGt2_5um);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-Count 2.5;Particulate Matter;PM;particles per .1 liter;ppl;0;1000;2000;3000;4000");
   btm_Serial.print(rawGt5_0um);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-Count 5;Particulate Matter;PM;particles per .1 liter;ppl;0;1000;2000;3000;4000");
   btm_Serial.print(rawGt10_0um);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-Count 10;Particulate Matter;PM;particles per .1 liter;ppl;0;1000;2000;3000;4000");
   btm_Serial.print(BAMPM2_5);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-BAM 2.5;Particulate Matter;PM;micrograms per cubic meter;ppl;0;12;35;55;150");
   btm_Serial.print(BAMPM10);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-BAM 10;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;12;35;55;150");
   btm_Serial.print(GRIMMPM1);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-GRIMM 1;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;12;35;55;150");
   btm_Serial.print(GRIMMPM2_5);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-GRIMM 2.5;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;12;35;55;150");
   btm_Serial.print(GRIMMPM10);
   btm_Serial.println(";AirBeam2:" + inData + ";AirBeam2-PM-GRIMM 10;Particulate Matter;PM;micrograms per cubic meter;µg/m³;0;12;35;55;150");
   */

}

// Function to Stream Wifi or Cell Data

void stream_wifi_cell_data(){
  if (mode == 1){ 
    starttime1 = millis();
    pms_average();
    wifi_Serial.listen();
    wifi_Serial.flush();
    //wifi_Serial.print("AT+SLEEP=0\r\n");   
    senddata(fah, 0);
    senddata(real_hum, 1);
    senddata((PM1_0), 2);
    senddata((PM2_5), 3);
    senddata((PM10_0), 4);
    /*senddata(int(rawGt0_3um), 5);
     senddata(int(rawGt0_5um), 6);
     senddata(int(rawGt1_0um), 7);
     senddata(int(rawGt2_5um), 8);
     senddata(int(rawGt5_0um), 9);
     senddata(int(rawGt10_0um), 10);
     senddata(int(BAMPM2_5), 11);
     senddata(int(BAMPM10), 12);
     senddata(int(GRIMMPM1), 13);
     senddata(int(GRIMMPM2_5), 14);
     senddata(int(GRIMMPM10), 15);*/
    //wifi_Serial.print("AT+SLEEP=1\r\n");
  }

  if (mode == 2){  //Cellular
    starttime1 = millis();
    pms_average();
    cellular_Serial.listen();
    cellular_Serial.flush();
    //digitalWrite(dtr_cell, LOW); 
    //cellular_Serial.print("AT+CSCLK=0\r\n");
    senddata(fah, 0);
    senddata(real_hum, 1);
    senddata((PM1_0), 2);
    senddata((PM2_5), 3);
    senddata((PM10_0), 4);
    /*senddata(int(rawGt0_3um), 5);
     senddata(int(rawGt0_5um), 6);
     senddata(int(rawGt1_0um), 7);
     senddata(int(rawGt2_5um), 8);
     senddata(int(rawGt5_0um), 9);
     senddata(int(rawGt10_0um), 10);
     senddata(int(BAMPM2_5), 11);
     senddata(int(BAMPM10), 12);
     senddata(int(GRIMMPM1), 13);
     senddata(int(GRIMMPM2_5), 14);
     senddata(int(GRIMMPM10), 15);*/
    //digitalWrite(dtr_cell, HIGH);
    //cellular_Serial.print("AT+CSCLK=1\r\n");
  }
}

// Function to Config Wifi when selected

void wifi_config(){ //WiFi Selected
  flag = 1; //One time setup for time sync
  btm_Serial.end(); 
  set_cell_baud_rate();    
  SMS_sleep();
  read_eeprom();
  set_ESP_baud_rate();
  i = 0;
  wifi_Serial.print("AT\r\n");
  while(!wifi_Serial.find("OK") && i++ < 10){
    wifi_Serial.print("AT\r\n"); //Might not work
  }
  if (i == 11){
    Serial.println(F("WiFi Not Good"));
  }
  else {
    Serial.println(F("WiFi Good"));
  }
  i = 0;
  wifi_Serial.print("AT+CIPMUX=0\r\n");
  while(!wifi_Serial.find("OK") && i++ < 10){ //Set mux
    wifi_Serial.print("AT+CIPMUX=0\r\n");
  }
  if (i == 11){
    Serial.println(F("Single IP Mode Not Enable"));
  }
  else {
    Serial.println(F("Single IP Mode Enable"));
  }
  i = 0;
  wifi_Serial.print("AT+CWMODE=1\r\n");
  while(!wifi_Serial.find("OK") && i++ < 10){ //Set mode
    wifi_Serial.print("AT+CWMODE=1\r\n");
  } 
  if (i == 11){
    Serial.println(F("Station Mode Not Enable"));
  }
  else {
    Serial.println(F("Station Mode Enable"));
  }
  i = 0;
  wifi_Serial.print("AT+CWJAP=\"" + ssid + "\",\"" + pass + "\"\r\n");
  while(!wifi_Serial.find("OK") && i++ < 10){  //Join netwok
    wifi_Serial.print("AT+CWJAP=\"" + ssid + "\",\"" + pass + "\"\r\n");
  }
  if (i == 11){
    Serial.println(F("Could Not Connect To WiFi Access Point"));
  }
  else {
    Serial.println(F("Connected to WiFi Access Point"));
  }
  WiFitimesync(); //Get time & date
  trigger = 1;  //Start streaming *

}

// Function to Config Cellular when selected

void cell_config(){
  flag = 1; //One time setup for time sync 
  btm_Serial.end();
  set_ESP_baud_rate();
  ESP_deep_sleep();
  read_eeprom();
  set_cell_baud_rate();
  i = 0;
  cellular_Serial.print("AT\r\n");
  while(!cellular_Serial.find("OK") && i++ < 10){
    cellular_Serial.print("AT\r\n");
  }
  if (i == 11){
    Serial.println(F("Cellular Not Good"));
  }
  else {
    Serial.println(F("Cellular Good"));
  }
  i = 0;
  cellular_Serial.print("AT+CIPMUX=0\r\n"); //Set TCP single connection
  while(!cellular_Serial.find("OK") && i++ < 10){
    cellular_Serial.print("AT+CIPMUX=0\r\n");
  }
  if (i == 11){
    Serial.println(F("Single IP Mode Not Enable"));
  }
  else {
    Serial.println(F("Single IP Mode Enable"));
  }
  //Serial.println(F("Done"));
  SetSMSTime();  
  trigger = 1;
}

void load_saved_config(){   
  if (mode_set_eeprom == 1){  //BTM
    mode = 0; 
    done = 0;
    flag = 0;
    trigger = 1;
  }   
  if (mode_set_eeprom==2){  //Wifi
    btm_Serial.begin(19200);
    delay(200);
    btm_Serial.listen();
    btm_Serial.println(F("BC:SP=01"));
    Serial.println(F("Bluetooth Asleep"));
    flag = 0;
    mode = 1;
    blue();
    reset_esp();
    wifi_config(); // Moved here from loop      
  }
  if (mode_set_eeprom==3){  //Cellular
    btm_Serial.begin(19200);
    delay(200);     
    btm_Serial.listen();
    btm_Serial.println(F("BC:SP=01"));
    Serial.println(F("Bluetooth Asleep"));
    flag = 0;
    mode = 2;
    blue();
    reset_cell();
    cell_config(); // Moved here from loop
  }
}

void Check_Modules(){
  uint8_t  i = 0;  
  if(btm_Serial.isListening()){
    Serial.print("Checking Bluetooth Module");
    do
    {
      Serial.print(F("."));
      i = i + 1;
      if(i > 5) {
        Serial.println(F("Error"));
        btm_Serial.end();
        i = 0;        
        break;
      }
      else{
        Serial.println(F("Good"));
        btm_Serial.end();
        i = 0;
        break;
      }
    }
    while(!btm_Serial.find("OK"));
  }
  if(wifi_Serial.isListening()){
    Serial.print("Checking WiFi Module");
    do
    {
      Serial.print(F("."));
      i = i + 1;
      if(i > 5) {
        Serial.println(F("Error"));
        wifi_Serial.end();
        i = 0;
        break;
      }
      else{
        Serial.println(F("Good"));
        wifi_Serial.end();
        i = 0;
        break;
      }
    }
    while(!wifi_Serial.find("OK"));
  }
  if(cellular_Serial.isListening()){
    Serial.print("Checking Cellular Module");
    do
    {
      cellular_Serial.print("AT\r\n");
      Serial.print(F("."));
      i = i + 1;
      //Serial.print(i);
      if(i > 5) {
        Serial.println(F("Error"));
        cellular_Serial.end();
        i = 0;
        break;
      }
      else{
        Serial.println(F("Good"));
        cellular_Serial.end();
        i = 0;
        break;
      }
    }
    while(!cellular_Serial.find("OK"));
  }
}
void setup() {
  Serial.begin(9600);
  /*Set pins*/
  pinMode(red_led, OUTPUT);
  pinMode(green_led, OUTPUT);
  pinMode(blue_led, OUTPUT);
  pinMode(btm_reset, OUTPUT);
  pinMode(wifi_reset, OUTPUT);
  pinMode(dtr_cell, OUTPUT);
  pinMode(cell_reset, OUTPUT);
  digitalWrite(cell_reset , HIGH);
  digitalWrite(red_led , HIGH);
  digitalWrite(green_led , HIGH);
  digitalWrite(blue_led , HIGH);
  digitalWrite(wifi_reset , HIGH);
  digitalWrite(dtr_cell , LOW);
  red();  
  reset_btm();
  delay(3000); 

  Serial.println(F("System Check"));  
  set_btm_baud_rate(); 
  btm_Serial.println(F("BC:SP=00")); //Check BTM
  Check_Modules();
  delay(200);
  set_ESP_baud_rate();
  wifi_Serial.print("AT\r\n"); //Check WiFi
  Check_Modules();
  delay(200);
  set_cell_baud_rate();
  cellular_Serial.print("AT\r\n");  //Check Cell
  Check_Modules();
  delay(200);
  cellular_Serial.begin(9600);
  cellular_Serial.print("AT+CGPSPWR=0\r\n"); //Turn GPS off
  while(!cellular_Serial.find("OK") && i++ < 5){
    cellular_Serial.print("AT+CGPSPWR=0\r\n");
  }
  if (i == 6){
    Serial.println(F("GPS Not Powered Down"));
  }
  else {
    Serial.println(F("GPS Powered Down"));
  }
  cellular_Serial.end();
  pt_Serial.begin(9600);
  delay(200); 
  Serial.print(F("Checking PMS."));
  pms_check();  //Check PMS 
  if (x == 0){
    while (x == 0){
      pms_check();
      delay(700);
    }
  }  
  Serial.print(F("Checking Temperature: "));
  int temp_analog_cel = analogRead(A7);
  int temp_cel = ((temp_analog_cel * (5.0 / 1023.0)) - 0.5) * 100;
  Serial.print(temp_cel);  //Check Temp
  Serial.println(F("C"));
  Serial.print(F("Checking Humidity: "));
  int temp_analog_hum = analogRead(A6);
  int temp_real_hum = round((((temp_analog_hum * (5.0 / 1023.0)) - 0.05) * 50) / (1.0546 - (0.00216 * temp_cel))); 
  Serial.print(temp_real_hum);  //Check Humidity
  Serial.println(F("%"));
  Serial.println(F("System Check Complete"));

  set_btm_baud_rate();

  btm_Serial.println(F("BC:FT=00,00,00,01,03,0000"));
  delay(200);
  btm_Serial.println(F("BC:AD"));
  delay(200);
  readBTMAC();
  inData.trim();  //Delete extra \r\n from string 
  btm_Serial.print(F("BC:NM=Airbeam2:"));
  btm_Serial.println(inData);
  Serial.print("Airbeam2 MAC Address: ");
  Serial.println(inData);
  Serial.println("Firmware: 5 Sensor Parameter");
  delay(200);  
  btm_Serial.println(F("BC:SP=00"));  //sleep enabled for Bluetooth
  delay(200);
  bt_buffer[0] = 0;
  mode_set_eeprom = EEPROM.read(0); // Saving EEPROM location 0 to use later in the code 
  Serial.print("Airbeam2 Status: ");
  if(mode_set_eeprom == 0 || mode_set_eeprom == 0xFF)
    Serial.println("Defaulted on Zero Configuration");
  if(mode_set_eeprom == 1)
    Serial.println("Currently on Bluetooth Configuration");
  if(mode_set_eeprom == 2)
    Serial.println("Currently on WiFi Configuration");
  if(mode_set_eeprom == 3)
    Serial.println("Currently on Cellular Configuration");
}

void loop() {
  if (config_window == true){ //Checks for configuration window is true it stops the currentMillis from counting to have infinite time to configure
    currentMillis = 0;
  }
  else{//If configuration window is false it will continue to count until it reaches 60000 or one minute
    currentMillis = millis();
  }

  if (currentMillis <= 60000){ //Updates bluetooth status within the one minute window to get information, did not use timer_expired because did not want the green light afterwards
    if(trigger == 0){
      green();
      update_bt_status();
    }
  }

  if ((currentMillis >= timer_expired) && trigger == 0){ //Changed timer_expired to 60000 with unsigned long variable to match with currentMillis
    if (mode_set_eeprom != 0 && mode_set_eeprom != 0xFF) { // Will only run if this shows that the eeprom has been configured, if not, it will set timer_expired to -1 and never run again (0xFF for brand new modules)
      load_saved_config(); 
    }
    trigger = 2;
    timer_expired = -1; // We only want run this function once
  }

  if (trigger == 1 && timer_expired != -1){  //Used for when the deice is done being reconfigured within the 1 min window. 
    mode_set_eeprom = EEPROM.read(0);
    load_saved_config();
    trigger = 2;
    timer_expired = -1;
  }

  if (minute() == 0 && (second() == 0 || second() == 30) && trigger == 2 && mode == 1){ //Daily Time Sync for WiFi, different seconds are used because the AB2 might be doing another process.
    blue();
    trigger = 3;
    wifi_Serial.listen();
    WiFitimesync();
    Serial.println(F("Daily WiFi Time Sync Complete!"));
    trigger = 2;
    off();
  }

  if(minute() == 0 && (second() == 0 || second() == 30) && trigger == 2 && mode == 2){ //Daily Time Sync for Cellular
    blue();
    trigger = 3;
    cellular_Serial.listen();
    cellular_Serial.print("AT+CCLK?\r\n");
    cellular_Serial.find("+CCLK: \"");
    cellular_Serial.readBytesUntil('"',replybuffer_,18);
    Serial.println(replybuffer_);
    Serial.println(F("Daily Cellular Time Sync Complete!"));
    years = atoi(strtok(replybuffer_, "/"));
    months = atoi(strtok(NULL, "/"));
    days = atoi(strtok(NULL, ","));
    hours = atoi(strtok(NULL, ":"));  
    mins = atoi(strtok(NULL, ":"));
    secs = atoi(strtok(NULL, "-"));
    setTime(hours, mins, secs, days, months, years);
    trigger = 2;
    off();
  }

  if((millis() - starttime0 >= 100)){
    starttime0 = millis();
    if(mode == 0){
      pms_bt(); 
    }
    else{
      pms(); 
    }
  }

  if(trigger == 2 && mode == 0){ //Query Bluetooth to check to see if it is connected or disconnected also has a clear bt_buffer to clear erroneous data.
    if(btm_Serial.available()){
      while(btm_Serial.available()){
        if(put_chr_buf(char(btm_Serial.read()))){
          //Serial.println(strlen(bt_buffer));
          //Serial.println(bt_buffer);
          if (strncmp(bt_buffer, "SS=00", 5) == 0 || strncmp(bt_buffer, "SS=10", 5) == 0 || strncmp(bt_buffer, "SS=20", 5) == 0 || strncmp(bt_buffer, "SS=30", 5) == 0 || strncmp(bt_buffer, "SS=40", 5) == 0 || strncmp(bt_buffer, "SS=50", 5) == 0) {
            //Serial.println(bt_buffer);
            Serial.println(F("Bluetooth Disconnected"));
            //Serial.println(strlen(bt_buffer));
            bt_buffer[0] = 0;
            bt_index = 0;
            bt_disconnected = true;
          }
          if(strncmp(bt_buffer, "SS=02", 5) == 0 || strncmp(bt_buffer, "SS=12", 5) == 0 || strncmp(bt_buffer, "SS=22", 5) == 0 || strncmp(bt_buffer, "SS=32", 5) == 0 || strncmp(bt_buffer, "SS=42", 5) == 0 || strncmp(bt_buffer, "SS=52", 5) == 0) {
            //Serial.println(bt_buffer);
            Serial.println(F("Bluetooth Connected"));
            //Serial.println(strlen(bt_buffer));
            bt_buffer[0] = 0;
            bt_index = 0;
            bt_disconnected = false;
          }
        }
      }
    }
   else{
      if(strlen(bt_buffer)>0){
      //Serial.println(bt_buffer);
      //Serial.println(strlen(bt_buffer));
      bt_buffer[0] = 0;
      bt_index = 0;
      }
    }
  }
  /*  
   if ((millis()-LED_on) >= 1000 && bt_disconnected == true && trigger == 2 && mode == 0){  //Turn on for one second
   LED_on = millis();
   off();
   }
   
   if ((millis()-LED_off) >= 4000 && bt_disconnected == true && trigger == 2 && mode == 0){  //Turn off for three seconds
   LED_off = millis();
   green();
   }
   */

  if((millis() - starttime2) >= 3000 && bt_disconnected == true && trigger == 2 && mode == 0){ //Trigger for when the Bluetooth LED to activate every 3 seconds.
    commLED = true;
    starttime2 = millis();
    stream_light = 2; //Trigger for white LED to turn back on after disconnections from Bluetooth.
  }

  if(bt_disconnected == true && trigger == 2 && mode == 0){ //When trigger from Bluetooth LED is activated, the LED turns on for 0.5 second then it toggles between on then off only until the next trigger from top 3 seconds.
    if((millis() - starttime2) <= 500 && trigger == 2 && commLED == true){
      //green();
      red();
    }
    else{
      commLED = false;
      off();
    }
  }
  
  if(stream_light == 2 && bt_disconnected == false){ //The statement to start the white LED after reconnection of Bluetooth.
    white();
    //blue();
    led_stream = millis();
    stream_light = 0;
  }

  if((millis() - starttime1) >= 1000 && trigger == 2 && mode == 0){ //Stream Over Bluetooth every second (not averaged)
    if(bt_disconnected == false){
    if(stream_light == 1){
      white();
      //blue();
      led_stream = millis();
      stream_light = 0;
    }
    stream_bt_data(); // Moved here from loop 
  }
  }

  if((millis() - starttime1) >= 60000 && trigger == 2 && (mode == 1 || mode == 2)){ //Stream Over Wifi or Cellular every minute averaged  
    if(stream_light == 1){
      white();
      //blue();  
      led_stream = millis();
      stream_light = 0;
    } 
    stream_wifi_cell_data(); //Moved here from loop
  }

  if((millis()-led_stream) >= 120000 && stream_light == 0){  //Turn off white LED after 30 seconds
    off();    
  }
}
























