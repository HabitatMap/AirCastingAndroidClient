#include <TimeLib.h>
#include <SoftwareSerial.h>
#include <WProgram.h>
#include "Time_Sync.h"
#include "Strings.h"

String datetime;

unsigned long begintime_;
unsigned long elapsedtime_;

extern int years;
extern int months;
extern int days;
extern int hours;
extern int mins;
extern int secs;
extern int Zone;

extern char buff[];
extern SoftwareSerial wifi_Serial;

bool USDST = true;

int printDigits(int digits)
{
  if (digits < 10)
    Serial.print(F("0"));
  return digits;
}

void TimeZonefromGMT(int Zone, int months, int days, int years, int hours, int mins) {
  hours = hours + Zone;
  if (hours < 1) {
    hours = hours + 24;
    if (hours == 24) {
      hours = 0;
    }
    else {
      days = days - 1;
    }
    if (days < 1) {
      months = months - 1;
      if (months == 0) {
        months = 12;
        days = 31;
        years = years - 1;
      }
      if (months == 1)
        days = 31;
      if (months == 2) {
        if (years % 4 == 0) {
          days = 29;
        }
        else
          days = 28;
      }
      if (months == 3)
        days = 31;
      if (months == 4)
        days = 30;
      if (months == 5)
        days = 31;
      if (months == 6)
        days = 30;
      if (months == 7)
        days = 31;
      if (months == 8)
        days = 31;
      if (months == 9)
        days = 30;
      if (months == 10)
        days = 31;
      if (months == 11)
        days = 30;
    }
  }
  if (hours > 23) {
    hours = hours - 24;
    if (((months == 1 && days == 31) || (months == 2 && days == 29 && years % 4 == 0) || (months == 2 && days == 28 && years % 4 != 0) || (months == 3 && days == 31) || (months == 4 && days == 30) || (months == 5 && days == 31) || (months == 6 && days == 30) || (months == 7 && days == 31) || (months == 8 && days == 31) || (months == 9 && days == 30) || (months == 10 && days == 31) || (months == 11 && days == 30) || (months == 12 && days == 31))) {
      months = months + 1;
      days = 1;
    }
    else
      days = days + 1;
  }

  if (USDST == true) {
    bool DSTflag = false;
    int y = years % 100;
    int x = (y + (y / 4) + 2) % 7;
    if (((months == 3 && days == (14 - x) && hours > 1)) || ((months == 3 && days > (14 - x)) || ((months > 3) && (months < 11)))) {
      DSTflag = true;
    }
    if (((months == 11 && days == (7 - x) && hours > 1)) || ((months == 11 && days == (7 - x)) || ((months < 3) && (months > 11)))) {
      DSTflag = false;
    }

    if (DSTflag == true)
    {
      hours = hours + 1;
    }
  }
  setTime(hours, mins, secs + ((elapsedtime_ - begintime_)/1000), days, months , years);
}

void WiFitimesync() {
  uint8_t  i = 0;
  String TimeGETrequestFull;
  String TimeGETrequest;
  //wifi_Serial.print("AT+CIPSTART=\"TCP\",\"www.aircasting.org\",80\r\n"); //Establish connection
  //OKprompter();
  open_TCP_WiFi();
  strcpy_P(buff, (char*)pgm_read_word(&(time_request_table[0])));
  TimeGETrequest = buff;
  TimeGETrequestFull = TimeGETrequest + TimeGETrequest.length();
  strcpy_P(buff, (char*)pgm_read_word(&(time_request_table[1])));
  TimeGETrequestFull += buff;
  wifi_Serial.print("AT+CIPSEND=" + String(TimeGETrequest.length()) + "\r\n");  //Send data
  while(!wifi_Serial.find(">") && i++ <5) {  // CHECK PM
    pms(); 
  }
  if (i == 6){
    Serial.println(F("\">\" Not Found"));
  }
  else {
    Serial.println(F("\">\" Found"));
  }
  //Arrowprompter();
  wifi_Serial.print(TimeGETrequestFull);
  wifi_Serial.print("\r\n");
  begintime_ = millis();
  Serial.print(F("Searching for Date: prompt."));
  i = 0;
  while (!wifi_Serial.find("Date: "))
  {
    pms();
    Serial.print(F("."));
    i = i + 1;
    if (i > 20) {
      Serial.print(F("Error!"));
      break;
    }
  }
  Serial.println(F("Done"));
  datetime = "";
  for (int i = 0; i < 25; i++)
  {
    pms();
    if (wifi_Serial.available())
    {
      char c = wifi_Serial.read();
      //Serial.write(c);
      datetime += String(c);
    }
    else i--;
  }

  years = datetime.substring(12, 16).toInt();
  if (datetime.substring(8, 11) == "Jan") {
    months = 1;
  }
  if (datetime.substring(8, 11) == "Feb") {
    months = 2;
  }
  if (datetime.substring(8, 11) == "Mar") {
    months = 3;
  }
  if (datetime.substring(8, 11) == "Apr") {
    months = 4;
  }
  if (datetime.substring(8, 11) == "May") {
    months = 5;
  }
  if (datetime.substring(8, 11) == "Jun") {
    months = 6;
  }
  if (datetime.substring(8, 11) == "Jul") {
    months = 7;
  }
  if (datetime.substring(8, 11) == "Aug") {
    months = 8;
  }
  if (datetime.substring(8, 11) == "Sep") {
    months = 9;
  }
  if (datetime.substring(8, 11) == "Oct") {
    months = 10;
  }
  if (datetime.substring(8, 11) == "Nov") {
    months = 11;
  }
  if (datetime.substring(8, 11) == "Dec") {
    months = 12;
  }
  days = datetime.substring(5, 7).toInt();
  hours = datetime.substring(17, 19).toInt();
  mins = datetime.substring(20, 22).toInt();
  secs = datetime.substring(23, 25).toInt();

  elapsedtime_ = millis();
  //Serial.println(((elapsedtime - begintime)/1000));
  TimeZonefromGMT(Zone, months, days, years, hours, mins);

  Serial.print(F("Set Date: "));
  Serial.print(printDigits(month()));
  Serial.print(F("/"));
  Serial.print(printDigits(day()));
  Serial.print(F("/"));
  Serial.print(year());
  Serial.print(F(" Set Time: "));
  Serial.print(printDigits(hour()));
  Serial.print(F(":"));
  Serial.print(printDigits(minute()));
  Serial.print(F(":"));
  Serial.println(printDigits(second()));
}





