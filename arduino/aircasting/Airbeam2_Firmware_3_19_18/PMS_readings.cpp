#include <WProgram.h>
#include "PMS.h"
#include <TimeLib.h>

#define LENG        31
unsigned char buf[LENG];
unsigned int ERROR_CODE = 0;


/*Temp variables for averaging*/
unsigned int tempconcPM1_0_CF1 = 0;
unsigned int tempconcPM2_5_CF1 = 0;
unsigned int tempconcPM10_0_CF1 = 0;
unsigned int tempconcPM1_0_amb = 0;
unsigned int tempconcPM2_5_amb = 0;
unsigned int tempconcPM10_0_amb = 0;
unsigned int temprawGt0_3um = 0;
unsigned int temprawGt0_5um = 0;
unsigned int temprawGt1_0um = 0;
unsigned int temprawGt2_5um = 0;
unsigned int temprawGt5_0um = 0;
unsigned int temprawGt10_0um = 0;

unsigned long analog_cel;
unsigned long analog_hum;
unsigned long pmscount = 0;
unsigned long tempcount = 0;
/*Global variables used for streaming (Used in main code)*/
extern unsigned int concPM1_0_CF1 ;
extern unsigned int concPM2_5_CF1 ;
extern unsigned int concPM10_0_CF1 ;
extern unsigned int concPM1_0_amb ;
extern unsigned int concPM2_5_amb ;
extern unsigned int concPM10_0_amb ;
extern unsigned int rawGt0_3um ;
extern unsigned int rawGt0_5um ;
extern unsigned int rawGt1_0um ;
extern unsigned int rawGt2_5um ;
extern unsigned int rawGt5_0um ;
extern unsigned int rawGt10_0um ;
extern unsigned int BAMPM10;
extern unsigned int BAMPM2_5;
extern unsigned int GRIMMPM10;
extern unsigned int GRIMMPM2_5;
extern unsigned int GRIMMPM1;

extern unsigned int PM1_0;
extern unsigned int PM2_5;
extern unsigned int PM10_0;

extern int fah; 
extern double cel;
extern int real_hum;
extern String inData;

extern int avgyears;
extern int avgmonths;
extern int avgdays;
extern int avghours;
extern int avgmins;
extern int avgsecs;

extern int x;  //Variable to check PMS

int print_Digits(int digits)
{
  if (digits < 10)
    Serial.print(F("0"));
  return digits;
}

/*Check PMS checksum*/
int isValid(unsigned char *buf)
{
  int checkSum = (buf[29] << 8) + buf[30];
  int sum = 0x42;
  for (int i = 0; i < 29; i++) {
    sum = sum + buf[i];
  }
  return sum == checkSum;
}

/*Function to check PMS*/
int pms_check(){
  if (pt_Serial.available() > 31) {
    char crap = pt_Serial.read();
    pt_Serial.readBytes(buf, LENG);    
    if (buf[0] == 0x4d && isValid(buf)) {
      Serial.println("Good");
      concPM1_0_CF1 = (buf[3] << 8) + buf[4];
      concPM2_5_CF1 = (buf[5] << 8) + buf[6];
      concPM10_0_CF1 = (buf[7] << 8) + buf[8];
      concPM1_0_amb = (buf[9] << 8) + buf[10];
      concPM2_5_amb = (buf[11] << 8) + buf[12];
      concPM10_0_amb = (buf[13] << 8) + buf[14];
      rawGt0_3um = (buf[15] << 8) + buf[16];
      rawGt0_5um = (buf[17] << 8) + buf[18];
      rawGt1_0um = (buf[19] << 8) + buf[20];
      rawGt2_5um = (buf[21] << 8) + buf[22];
      rawGt5_0um = (buf[23] << 8) + buf[24];
      rawGt10_0um = (buf[25] << 8) + buf[26];
      ERROR_CODE = buf[28];  
      x = 1; 
      return x;      
    }    
    else{
      Serial.print(F("."));
      while (pt_Serial.read() != -1) {
      };
      x = 0;      
      return x;
    }   
  }
  else {
    Serial.print(F("."));
    x = 0;
    return x;
  }
}

/*PMS reading function*/
void pms() {
  tempcount++;
  analog_cel += analogRead(A7);
  analog_hum += analogRead(A6);

  if (pt_Serial.available() > 31) {
    char crap = pt_Serial.read();
    pt_Serial.readBytes(buf, LENG);
    if (buf[0] == 0x4d && isValid(buf)) {
      /*Serial.print("P");*/
      pmscount++;    
      tempconcPM1_0_CF1 += (buf[3] << 8) + buf[4];
      tempconcPM2_5_CF1 += (buf[5] << 8) + buf[6];
      tempconcPM10_0_CF1 += (buf[7] << 8) + buf[8];
      tempconcPM1_0_amb += (buf[9] << 8) + buf[10];
      tempconcPM2_5_amb += (buf[11] << 8) + buf[12];
      tempconcPM10_0_amb += (buf[13] << 8) + buf[14];
      temprawGt0_3um += (buf[15] << 8) + buf[16];
      temprawGt0_5um += (buf[17] << 8) + buf[18];
      temprawGt1_0um += (buf[19] << 8) + buf[20];
      temprawGt2_5um += (buf[21] << 8) + buf[22];
      temprawGt5_0um += (buf[23] << 8) + buf[24];
      temprawGt10_0um += (buf[25] << 8) + buf[26];
      ERROR_CODE = buf[28];

      /*Enable to see Temp values*/
      /* 
      Serial.print(F("Date:"));
      Serial.print(print_Digits(month()));
      Serial.print(F("/"));
      Serial.print(print_Digits(day()));
      Serial.print(F("/"));
      Serial.print(year());
      Serial.print(F(" Time:"));
      Serial.print(print_Digits(hour()));
      Serial.print(F(":"));
      Serial.print(print_Digits(minute()));
      Serial.print(F(":"));
      Serial.print(print_Digits(second()));
      Serial.print(" TemperatureCounts:");
      Serial.print(tempcount);
      Serial.print(" AnalogVoltageCel:");
      Serial.print(analog_cel);
      Serial.print(" AnalogVoltageHum:");
      Serial.print(analog_hum);
      Serial.print(" PlantowerCounts:");
      Serial.print(pmscount);
      Serial.print(" ");
      Serial.print(tempconcPM1_0_amb);
      Serial.print(";");
      Serial.print(tempconcPM2_5_amb);
      Serial.print(";");
      Serial.println(tempconcPM10_0_amb);
      */
    }
    else{
      /*Serial.println("Checksum or packet not valid");*/
      while (pt_Serial.read() != -1) {
      };      
      return;
    }   
  }
  else {
    /*Serial.println("Packet < 31");*/
    return;
  }
  delayMicroseconds(10);
}

/*PMS reading function*/
void pms_bt() {  // Essentially the same as pms(), but with averaging removed 

  analog_cel = analogRead(A7);
  analog_hum = analogRead(A6);

  cel = ((analog_cel * (5.0 / 1023.0)) - 0.5) * 100;
  fah = round((cel * 1.8)  + 32);  
  //hum = (((analog_hum * (5.0 / 1023.0)) - 0.05) * 50);
  real_hum = round((((analog_hum * (5.0 / 1023.0)) - 0.05) * 50) / (1.0546 - (0.00216 * cel))); 

  if (pt_Serial.available() > 31) {
    char crap = pt_Serial.read();
    pt_Serial.readBytes(buf, LENG);    
    if (buf[0] == 0x4d && isValid(buf)) {
      /*Serial.println("Packet");*/
      concPM1_0_CF1 = (buf[3] << 8) + buf[4];
      concPM2_5_CF1 = (buf[5] << 8) + buf[6];
      concPM10_0_CF1 = (buf[7] << 8) + buf[8];
      concPM1_0_amb = (buf[9] << 8) + buf[10];
      concPM2_5_amb = (buf[11] << 8) + buf[12];
      concPM10_0_amb = (buf[13] << 8) + buf[14];
      rawGt0_3um = (buf[15] << 8) + buf[16];
      rawGt0_5um = (buf[17] << 8) + buf[18];
      rawGt1_0um = (buf[19] << 8) + buf[20];
      rawGt2_5um = (buf[21] << 8) + buf[22];
      rawGt5_0um = (buf[23] << 8) + buf[24];
      rawGt10_0um = (buf[25] << 8) + buf[26];
      ERROR_CODE = buf[28];  
    }    
    else{
      /*Serial.println("Checksum or packet not valid");*/
      while (pt_Serial.read() != -1) {
      };      
      return;
    }   
  }
  else {
    /*Serial.println("Packet < 31");*/
    return;
  }

  PM10_0 = 1.06*concPM10_0_amb;
  PM2_5 = 1.33*pow(concPM2_5_amb, 0.85);
  PM1_0 = 0.66776*pow(concPM1_0_amb, 1.1);

  /*Serial Monitor Output*/
  /*Serial.print(F("Date: "));
  Serial.print(print_Digits(avgmonths));
  Serial.print(F("/"));
  Serial.print(print_Digits(avgdays));
  Serial.print(F("/"));
  Serial.print(avgyears);
  Serial.print(F(" Time: "));
  Serial.print(print_Digits(avghours));
  Serial.print(F(":"));
  Serial.print(print_Digits(avgmins));
  Serial.print(F(":"));
  Serial.print(print_Digits(avgsecs));
  Serial.print(" TemperatureCounts:");
  Serial.print(tempcount); 
  Serial.print(" PlantowerCounts:");
  Serial.print(pmscount);
  Serial.print(" ");*/
  Serial.print("AirBeam2MAC: ");
  Serial.print(inData);
  Serial.print(" ");
  Serial.print(fah);
  Serial.print("F");
  Serial.print(" ");
  Serial.print(round(cel));
  Serial.print("C");
  Serial.print(" ");
  Serial.print(real_hum);
  Serial.print("RH");
  Serial.print(" ");
  Serial.print("PM-Amb1:");
  Serial.print(concPM1_0_amb);
  Serial.print(" ");
  Serial.print("PM-Amb2.5:");
  Serial.print(concPM2_5_amb);
  Serial.print(" ");
  Serial.print("PM-Amb10:");
  Serial.print(concPM10_0_amb);
  Serial.print(" ");
  Serial.print("PM1:");
  Serial.print(PM1_0);
  Serial.print(" ");
  Serial.print("PM2.5:");
  Serial.print(PM2_5);
  Serial.print(" ");
  Serial.print("PM10:");
  Serial.println(PM10_0); 
}

void pms_average() { //Average the number of readings taken
  //cel = ((analog_cel * 5.0 / 1023.0) - 0.5) / 0.01;
  cel = (((analog_cel/tempcount) * (5.0 / 1023.0)) - 0.5) * 100;
  fah = round((cel * 1.8)  + 32);

  //hum_vout = analogRead(A6) * (5.0 / 1024.0);  //A6 Humidity
  //hum = ((hum_vout/2.6)-0.1515)/0.00636;// Using equation
  //hum = (hum_vout-0.05)/0.02; // Using Graph

  //hum = (((analog_hum/tempcount) * (5.0 / 1023.0)) - 0.05) * 50;
  real_hum = round((((analog_hum/tempcount * (5.0 / 1023.0)) - 0.05) * 50) / (1.0546 - (0.00216 * cel))); 

  concPM1_0_CF1 = tempconcPM1_0_CF1 / pmscount;
  concPM2_5_CF1 = tempconcPM2_5_CF1 / pmscount;
  concPM10_0_CF1 = tempconcPM10_0_CF1 / pmscount;
  concPM1_0_amb = tempconcPM1_0_amb / pmscount;
  concPM2_5_amb = tempconcPM2_5_amb / pmscount;
  concPM10_0_amb = tempconcPM10_0_amb / pmscount;
  rawGt0_3um = temprawGt0_3um / pmscount;
  rawGt0_5um = temprawGt0_5um / pmscount;
  rawGt1_0um = temprawGt1_0um / pmscount;
  rawGt2_5um = temprawGt2_5um / pmscount;
  rawGt5_0um = temprawGt5_0um / pmscount;
  rawGt10_0um = temprawGt10_0um / pmscount;
  BAMPM2_5 = 0.744 * concPM2_5_amb;
  BAMPM10 = -30.436 * pow(concPM10_0_amb, 0.5) + 43.108 * pow(concPM2_5_amb, 0.5);
  GRIMMPM2_5 = 0.80243 * concPM2_5_amb;
  GRIMMPM1 = 0.75906 * concPM1_0_amb + 0.00645 * pow(concPM1_0_amb, 2);
  GRIMMPM10 = 6.1931 * pow(concPM10_0_amb, 0.33) - 1.1741 * concPM2_5_amb + 1.629 * concPM1_0_amb;

/*
PM10
y = 1.06Amb10
PM2.5
y = 1.33Amb2.5^0.85
PM1
y = 0.66776Amb1^1.1
*/

  PM10_0 = 1.06*concPM10_0_amb;
  PM2_5 = 1.33*pow(concPM2_5_amb, 0.85);
  PM1_0 = 0.66776*pow(concPM1_0_amb, 1.1);

  avgyears = year();
  avgmonths = month();
  avgdays = day();
  avghours = hour();
  avgmins = minute();
  avgsecs = second();

  /*Serial Monitor Output*/
  Serial.print(F("Date: "));
  Serial.print(print_Digits(avgmonths));
  Serial.print(F("/"));
  Serial.print(print_Digits(avgdays));
  Serial.print(F("/"));
  Serial.print(avgyears);
  Serial.print(F(" Time: "));
  Serial.print(print_Digits(avghours));
  Serial.print(F(":"));
  Serial.print(print_Digits(avgmins));
  Serial.print(F(":"));
  Serial.print(print_Digits(avgsecs));
  Serial.print(" TemperatureCounts:");
  Serial.print(tempcount); 
  Serial.print(" PlantowerCounts:");
  Serial.print(pmscount);
  Serial.print(" AirBeam2MAC: ");
  Serial.print(inData);
  Serial.print(" ");
  Serial.print(fah);
  Serial.print("F");
  Serial.print(" ");
  Serial.print(round(cel));
  Serial.print("C");
  Serial.print(" ");
  Serial.print(real_hum);
  Serial.print("RH");
  Serial.print(" ");
  Serial.print("PM-Amb1:");
  Serial.print(concPM1_0_amb);
  Serial.print(" ");
  Serial.print("PM-Amb2.5:");
  Serial.print(concPM2_5_amb);
  Serial.print(" ");
  Serial.print("PM-Amb10:");
  Serial.print(concPM10_0_amb);
  Serial.print(" ");
  Serial.print("PM1:");
  Serial.print(PM1_0);
  Serial.print(" ");
  Serial.print("PM2.5:");
  Serial.print(PM2_5);
  Serial.print(" ");
  Serial.print("PM10:");
  Serial.println(PM10_0);   
  
  analog_cel = 0;
  analog_hum = 0;
  tempconcPM1_0_CF1 = 0;
  tempconcPM2_5_CF1 = 0;
  tempconcPM10_0_CF1 = 0;
  tempconcPM1_0_amb = 0;
  tempconcPM2_5_amb = 0;
  tempconcPM10_0_amb = 0;
  temprawGt0_3um = 0;
  temprawGt0_5um = 0;
  temprawGt1_0um = 0;
  temprawGt2_5um = 0;
  temprawGt5_0um = 0;
  temprawGt10_0um = 0;
  tempcount = 0;
  pmscount = 0;
}






