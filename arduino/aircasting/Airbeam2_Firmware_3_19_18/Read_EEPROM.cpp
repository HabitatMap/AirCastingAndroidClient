#include <EEPROM.h>
#include <WProgram.h>
#include "Read_EEPROM.h"

int len; 
int offset;

extern String ssid ;
extern String pass ;
extern String uuid ;
extern String uuidauth ;
extern String longitude ;
extern String latitude ;
//extern String sensor_package ;
extern String zone ;
extern int Zone;
extern int mode;

void read_eeprom(){
  /*Clear string to avoid overlapping*/
  ssid = "";
  pass = "";
  uuid = "";
  uuidauth = "";
  longitude = "";   
  latitude = "";
  zone = "";
  
  len = EEPROM.read(1);
  offset = 8;
    
  for (int i=0; i<len; i++){
    char c = EEPROM.read(offset+i);
    uuid+=c;       
  }
  Serial.print("UUID: ");
  Serial.println(uuid);
 
  len = EEPROM.read(2);
  offset = 8 + EEPROM.read(1);
  
  for (int i=0; i<len; i++){
    char c = EEPROM.read(offset+i);
    uuidauth+=c;       
  }
  Serial.print("UUID Authentication: ");  
  Serial.println(uuidauth);
  
  len = EEPROM.read(3);
  offset = 8 + EEPROM.read(1)+EEPROM.read(2); 
  
  for (int i=0; i<len; i++){
    char c = EEPROM.read(offset+i);
    longitude+=c;       
  }
  Serial.print("Longitude: ");
  Serial.println(longitude);
  
  len = EEPROM.read(4);
  offset = 8+EEPROM.read(1)+EEPROM.read(2)+EEPROM.read(3);
  
  for (int i=0; i<len; i++){
    char c = EEPROM.read(offset+i);
    latitude+=c;      
  }
  Serial.print("Latitude: ");
  Serial.println(latitude);
  
  /*Read these only if Wifi was selected*/
    if (mode == 1){ 
    
    len = EEPROM.read(5);
    offset = 8+EEPROM.read(1)+EEPROM.read(2)+EEPROM.read(3)+EEPROM.read(4);
    
    for (int i=0; i<len; i++){
      char c = EEPROM.read(offset+i);
      ssid+=c;       
    }
    Serial.print("SSID: ");
    Serial.println(ssid); 
    
    len = EEPROM.read(6);
    offset = 8 + EEPROM.read(1)+EEPROM.read(2)+EEPROM.read(3)+EEPROM.read(4)+EEPROM.read(5);

    for (int i=0; i<len; i++){
      char c = EEPROM.read(offset+i);
      pass+=c;      
    }
    Serial.println(pass);
    
    len = EEPROM.read(7);
    offset = 8+EEPROM.read(1)+EEPROM.read(2)+EEPROM.read(3)+EEPROM.read(4)+EEPROM.read(5)+EEPROM.read(6);
    
    for (int i=0; i<len; i++){
      char c = EEPROM.read(offset+i);
      zone+=c;      
    }
    Zone = zone.toInt();
    Serial.print("Time Zone: ");
    Serial.println(Zone);
  }
}
  
