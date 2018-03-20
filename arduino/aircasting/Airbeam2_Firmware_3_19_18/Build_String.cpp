#include <WProgram.h>
#include "Strings.h"

extern String post_data;
extern String ssid ;
extern String pass ;
extern String uuid ;
extern String uuidauth ;
extern String longitude ;
extern String latitude ;
extern String inData ;

extern char buff[];

String printDigitsString(String digits){
  //int num = atoi(digits);
  if (digits.toInt() < 10)
    digits = "0" + digits;
  return digits;
}

/*Build post String*/
void getPost() {
  // post_data="";
  strcpy_P(buff, (char*)pgm_read_word(&(post_table[0])));
  post_data += buff;
  post_data += uuidauth;
  strcpy_P(buff, (char*)pgm_read_word(&(post_table[1]))); //PostData
  post_data += buff;
  //Serial.println(post_data);
  //return post_data;
}

/*Build Data 1 String*/
void getData_1(int stream) {
  // post_data="";
  strcpy_P(buff, (char*)pgm_read_word(&(data_1_table[0])));
  post_data += buff;
  switch (stream) {
    case 0:
      post_data += "Temperature";
      break;

    case 1:
      post_data += "Humidity";
      break;

    case 2:
      post_data += "Particulate Matter";
      break;

    case 3:
      post_data += "Particulate Matter";
      break;

    case 4:
      post_data += "Particulate Matter";
      break;

    case 5:
      post_data += "Particulate Matter";
      break;

    case 6:
      post_data += "Particulate Matter";
      break;

    case 7:
      post_data += "Particulate Matter";
      break;

    case 8:
      post_data += "Particulate Matter";
      break;

    case 9:
      post_data += "Particulate Matter";
      break;

    case 10:
      post_data += "Particulate Matter";
      break;

    case 11:
      post_data += "Particulate Matter";
      break;

    case 12:
      post_data += "Particulate Matter";
      break;

    case 13:
      post_data += "Particulate Matter";
      break;

    case 14:
      post_data += "Particulate Matter";
      break;

    case 15:
      post_data += "Particulate Matter";
      break;
  }
  strcpy_P(buff, (char*)pgm_read_word(&(data_1_table[1])));
  post_data += buff;
  post_data += longitude;
  strcpy_P(buff, (char*)pgm_read_word(&(data_1_table[2])));
  post_data += buff;
  post_data += latitude;
  strcpy_P(buff, (char*)pgm_read_word(&(data_1_table[3])));
  post_data += buff;
  //Serial.print(post_data);
  //return data_1;
}

/*Build Data2 String */
void getData_2(int stream) {
  // post_data="";
  strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[0])));
  post_data += buff;
  post_data += "Airbeam2-";
  post_data += inData;
  strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[1])));
  post_data += buff;
  switch (stream) {
    case 0:
      post_data += "F";
      break;

    case 1:
      post_data += "RH";
      break;

    case 2:
      post_data += "PM1";
      break;

    case 3:
      post_data += "PM2.5";
      break;

    case 4:
      post_data += "PM10";
      break;

    case 5:
      post_data += "PM-Count 0.3";
      break;

    case 6:
      post_data += "PM-Count 0.5";
      break;

    case 7:
      post_data += "PM-Count 1";
      break;

    case 8:
      post_data += "PM-Count 2.5";
      break;

    case 9:
      post_data += "PM-Count 5";
      break;

    case 10:
      post_data += "PM-Count 10";
      break;

    case 11:
      post_data += "PM-Bam 2.5";
      break;

    case 12:
      post_data += "PM-Bam 10";
      break;

    case 13:
      post_data += "PM-Grimm 1";
      break;

    case 14:
      post_data += "PM-Grimm 2.5";
      break;

    case 15:
      post_data += "PM-Grimm 10";
      break;
  }
  strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[2])));
  post_data += buff;
  post_data += uuid;
  strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[3])));
  post_data += buff;
  switch (stream) {
    case 0:
      post_data += "F";
      break;

    case 1:
      post_data += "RH";
      break;

    case 2:
      post_data += "PM";
      break;

    case 3:
      post_data += "PM";
      break;

    case 4:
      post_data += "PM";
      break;

    case 5:
      post_data += "PM";
      break;

    case 6:
      post_data += "PM";
      break;

    case 7:
      post_data += "PM";
      break;

    case 8:
      post_data += "PM";
      break;

    case 9:
      post_data += "PM";
      break;

    case 10:
      post_data += "PM";
      break;

    case 11:
      post_data += "PM";
      break;

    case 12:
      post_data += "PM";
      break;

    case 13:
      post_data += "PM";
      break;

    case 14:
      post_data += "PM";
      break;

    case 15:
      post_data += "PM";
      break;
  }
  strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[4])));
  post_data += buff;
  switch (stream) {
    case 0:
      post_data += "F";
      break;

    case 1:
      post_data += "%";
      break;

    case 2:
      post_data += "µg/m³";
      break;

    case 3:
      post_data += "µg/m³";
      break;

    case 4:
      post_data += "µg/m³";
      break;

    case 5:
      post_data += "ppl";
      break;

    case 6:
      post_data += "ppl";
      break;

    case 7:
      post_data += "ppl";
      break;

    case 8:
      post_data += "ppl";
      break;

    case 9:
      post_data += "ppl";
      break;

    case 10:
      post_data += "ppl";
      break;

    case 11:
      post_data += "µg/m³";
      break;

    case 12:
      post_data += "µg/m³";
      break;

    case 13:
      post_data += "µg/m³";
      break;

    case 14:
      post_data += "µg/m³";
      break;

    case 15:
      post_data += "µg/m³";
      break;
  }
  if (stream == 0) {
    strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[5])));
  }
  if (stream == 1) {
    strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[6])));
  }
  if (stream == 15 || stream == 14 || stream == 13 || stream == 12 || stream == 11 || stream == 3 || stream == 2) {
    strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[7])));
  }
  if (stream == 10 || stream == 9 || stream == 8 || stream == 7 || stream == 6 || stream == 5) {
    strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[8])));
  }
  if (stream == 4) {
    strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[9])));
  }

  post_data += buff;
  switch (stream) {
    case 0:
      post_data += "fahrenheit";
      break;

    case 1:
      post_data += "percent";
      break;

    case 2:
      post_data += "microgram per cubic meter";
      break;

    case 3:
      post_data += "microgram per cubic meter";
      break;

    case 4:
      post_data += "microgram per cubic meter";
      break;

    case 5:
      post_data += "particles per .1 liter";
      break;

    case 6:
      post_data += "particles per .1 liter";
      break;

    case 7:
      post_data += "particles per .1 liter";
      break;

    case 8:
      post_data += "particles per .1 liter";
      break;

    case 9:
      post_data += "particles per .1 liter";
      break;

    case 10:
      post_data += "particles per .1 liter";
      break;

    case 11:
      post_data += "microgram per cubic meter";
      break;

    case 12:
      post_data += "microgram per cubic meter";
      break;

    case 13:
      post_data += "microgram per cubic meter";
      break;

    case 14:
      post_data += "microgram per cubic meter";
      break;

    case 15:
      post_data += "microgram per cubic meter";
      break;
  }
  strcpy_P(buff, (char*)pgm_read_word(&(data_2_table[10])));
  post_data += buff;
  //Serial.print(post_data);
  //return data_2;
}

/*Build Data3 String*/
void getData_3(int years, int months, int days, int hours, int mins, int secs, int sensor_value) {
  //post_data="";
  post_data += years;
  post_data += "-";
  post_data += printDigitsString(String(months));
  post_data += "-";
  post_data += printDigitsString(String(days));
  post_data += "T";
  post_data += printDigitsString(String(hours));
  post_data += ":";
  post_data += printDigitsString(String(mins));
  post_data += ":";
  post_data += printDigitsString(String(secs));
  strcpy_P(buff, (char*)pgm_read_word(&(data_3_table[0])));
  post_data += buff;
  post_data += sensor_value;
  post_data += ",\\\"value\\\":";
  post_data += sensor_value;
  //Serial.print(post_data);
  //return data_3;
}



