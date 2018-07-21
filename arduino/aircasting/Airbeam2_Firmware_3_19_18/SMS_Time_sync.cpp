#include <TimeLib.h>
#include <SoftwareSerial.h>
#include <WProgram.h>
#include "Time_Sync.h"
#include "Color.h";

extern SoftwareSerial cellular_Serial;

char replybuffer[17];  
char c;

extern int years;
extern int months;
extern int days;
extern int hours;
extern int mins;
extern int secs;
extern int trigger;
extern int conn_stat;

int printDigits_(int digits)
{
  if (digits < 10)
    Serial.print(F("0"));
  return digits;
}

int printDigits_year_(int digit)
{
  Serial.print("20");
  return digit;
}

void ShowSerialData()
{
  while(cellular_Serial.available()!=0)
    Serial.print(cellular_Serial.read()); 
}

void SetSMSTime(){
  Serial.println(F("Setting time"));
  cellular_Serial.print("AT+COPS=2\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+COPS=2\r\n");
  }
  Serial.println("Done");

  cellular_Serial.print("AT+CLTS=1\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+CLTS=1\r\n");
  }
  Serial.println("Done");

  cellular_Serial.print("AT+COPS=0\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+COPS=0\r\n");
  }
  Serial.println("Done");
  cellular_Serial.print("AT+CCLK?\r\n");
  cellular_Serial.find("+CCLK: \"");
  cellular_Serial.readBytesUntil('"',replybuffer,18);

  //Serial.println(replybuffer);

  years = atoi(strtok(replybuffer, "/"));
  months = atoi(strtok(NULL, "/"));
  days = atoi(strtok(NULL, ","));
  hours = atoi(strtok(NULL, ":"));  
  mins = atoi(strtok(NULL, ":"));
  secs = atoi(strtok(NULL, "-"));
  setTime(hours, mins, secs, days, months, years);
  Serial.print(F("Set Date: "));
  Serial.print(printDigits_(int(months)));
  Serial.print(F("/"));
  Serial.print(printDigits_(int(days)));
  Serial.print(F("/"));
  Serial.println(printDigits_year_(int(years)));
  Serial.print(F("Set Time: "));
  Serial.print(printDigits_(int(hours)));
  Serial.print(F(":"));
  Serial.print(printDigits_(int(mins)));
  Serial.print(F(":"));
  Serial.println(printDigits_(int(secs)));
  Serial.println(F("\r\nSetting TCP"));

  cellular_Serial.print("AT+CIPSPRT=1\r\n");
  while(!cellular_Serial.find("OK")){;
    pms();
    cellular_Serial.print("AT+CIPSPRT=1\r\n");
  }
  Serial.println(F("Done"));

  cellular_Serial.print("AT+CIPSHUT\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+CIPSHUT\r\n");
  }
  Serial.println(F("Done"));

  cellular_Serial.print("AT+CGATT=1\r\n");
  while(!cellular_Serial.find("OK")){
    pms(); 
    cellular_Serial.print("AT+CGATT=1\r\n");
  }
  Serial.println(F("Done"));

  cellular_Serial.print("AT+SAPBR=3,1,\"CONTYPE\",\"GPRS\"\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+SAPBR=3,1,\"CONTYPE\",\"GPRS\"\r\n");
  }
  Serial.println(F("Done"));

  cellular_Serial.print("AT+SAPBR=3,1,\"APN\",\"FONAnet\"\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+SAPBR=3,1,\"APN\",\"FONAnet\"\r\n");
  }
  Serial.println(F("Done"));

  cellular_Serial.print("AT+CSTT=\"FONAnet\"\r\n");//cellular_Serial.print("AT+CSTT=\"FONAnet\"\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+CSTT=\"FONAnet\"\r\n");//cellular_Serial.print("AT+CSTT=\"FONAnet\"\r\n");
  }
  Serial.println(F("Done"));

  cellular_Serial.print("AT+SAPBR=1,1\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+SAPBR=1,1\r\n");
  }
  Serial.println(F("Done"));

  cellular_Serial.print("AT+CIICR\r\n");
  while(!cellular_Serial.find("OK")){
    pms();
    cellular_Serial.print("AT+CIICR\r\n");
  }
  Serial.println(F("Done"));  

  cellular_Serial.print("AT+CIFSR\r\n");
  //ShowSerialData();
  
  if (cellular_Serial.find("ERROR")){
    Serial.println("Bad Setup"); 
  }
  else{
    //ShowSerialData();
  }
}

