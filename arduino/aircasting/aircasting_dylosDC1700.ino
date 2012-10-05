//I. Heng and R. Yap
char line[80];
int small = 0;
int large = 0;
int i =0;

void setup()  {
  
  // set the data rate for the SoftwareSerial port
  Serial1.begin(9600);
  Serial2.begin(115200);
  
  //set up Arduino's Serial
  Serial.begin(9600);
  Serial.println("Ready");
}
void loop() {

  // listen for new serial coming in:
  char someChar = Serial1.read();

  if( someChar != -1){
    //Serial.print(someChar);
    if (someChar == '\n')
    {
      line[i++]=0;
      sscanf(line,"%d,%d",&small,&large);
      i=0;
      Serial.print(small);
      Serial.print(" : 0.5 Micron  ");
      Serial.print(large);
      Serial.println(" : 2.5 Micron");

      Serial2.print(small);
      Serial2.print(";CityTech56789;DC1700a;Small Particle;SP;0.5 Micron;Micron;0;1000;2000;3000;4000");
      Serial2.print("\n"); 

      Serial2.print(large);
      Serial2.print(";CityTech56789;DC1700b;Large Particle;LP;2.5 Micron;Micron;0;1000;2000;3000;4000");
      Serial2.print("\n");      
    }
    else
      line[i++] = someChar;
  }
}

