#include "Color.h"
#include "Arduino.h"

void red() {
  digitalWrite(red_led, LOW);
  digitalWrite(green_led, HIGH);
  digitalWrite(blue_led, HIGH);
}

void green() {
  digitalWrite(red_led, HIGH);
  digitalWrite(green_led, LOW);
  digitalWrite(blue_led, HIGH);
}

void blue() {
  digitalWrite(red_led, HIGH);
  digitalWrite(green_led, HIGH);
  digitalWrite(blue_led, LOW);
}

void magenta() {
  digitalWrite(red_led, LOW);
  digitalWrite(green_led, HIGH);
  digitalWrite(blue_led, LOW); 
}

void cyan(){
  digitalWrite(red_led, HIGH);
  digitalWrite(green_led, LOW);
  digitalWrite(blue_led, LOW);  
}

void yellow(){
  digitalWrite(red_led, LOW);
  digitalWrite(green_led, LOW);
  digitalWrite(blue_led, HIGH);  
}

void white(){
  digitalWrite(red_led, LOW);
  digitalWrite(green_led, LOW);
  digitalWrite(blue_led, LOW);
}

void off() {
  digitalWrite(red_led, HIGH);
  digitalWrite(green_led, HIGH);
  digitalWrite(blue_led, HIGH);
}

