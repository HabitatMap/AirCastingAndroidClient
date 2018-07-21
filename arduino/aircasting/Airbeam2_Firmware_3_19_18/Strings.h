#include <avr/pgmspace.h>

/*Progmem Stuff*/

//Post Data
const char post [] PROGMEM = {"POST /api/realtime/measurements.json HTTP/1.1\nHost: aircasting.org\nAuthorization: Basic "};
//Add uuidauth
const char post_1 [] PROGMEM = {"\nContent-Type: application/json\nAccept: application/json\nContent-Length: "};

//Data_1
const char data1 [] PROGMEM = {"{\"data\":\"{\\\"measurement_type\\\":\\\""};
//Add Particulate Matter || Temperature || Humidity
const char data1_1 [] PROGMEM = {"\\\", \\\"measurements\\\":[{\\\"longitude\\\":"};
//Add Longitude
const char data1_2 [] PROGMEM = {",\\\"latitude\\\":"};
//Add Latitude
const char data1_3 [] PROGMEM = { ",\\\"time\\\":\\\""};

//Data_2
const char data2 [] PROGMEM = {"}], \\\"sensor_package_name\\\":\\\""};
//Add Sensor Package
const char data2_1 [] PROGMEM = {"\\\", \\\"sensor_name\\\":\\\"AirBeam2-"};
//Add F || RH || PM
const char data2_2 [] PROGMEM = {"\\\", \\\"session_uuid\\\":\\\""};
//Add UUID
const char data2_3 [] PROGMEM = {"\\\", \\\"measurement_short_type\\\":\\\""};
//Add F || RH || PM
const char data2_4 [] PROGMEM = {"\\\", \\\"unit_symbol\\\":\\\""};
//Add F || ug/m3 || %
const char data2_5 [] PROGMEM = {"\\\", \\\"threshold_high\\\":105, \\\"threshold_low\\\":45, \\\"threshold_medium\\\":75, \\\"threshold_very_high\\\":135, \\\"threshold_very_low\\\":15, \\\"unit_name\\\":\\\""}; //Use this for temperature
const char data2_6 [] PROGMEM = {"\\\", \\\"threshold_high\\\":75, \\\"threshold_low\\\":25, \\\"threshold_medium\\\":50, \\\"threshold_very_high\\\":100, \\\"threshold_very_low\\\":0, \\\"unit_name\\\":\\\""};  //Use this for humidity
const char data2_7 [] PROGMEM = {"\\\", \\\"threshold_high\\\":55, \\\"threshold_low\\\":12, \\\"threshold_medium\\\":35, \\\"threshold_very_high\\\":150, \\\"threshold_very_low\\\":0, \\\"unit_name\\\":\\\""};  //Use this for PM
const char data2_8 [] PROGMEM = {"\\\", \\\"threshold_high\\\":3000, \\\"threshold_low\\\":1000, \\\"threshold_medium\\\":2000, \\\"threshold_very_high\\\":4000, \\\"threshold_very_low\\\":0, \\\"unit_name\\\":\\\""};  //Use this for PM different heat legend
const char data2_9 [] PROGMEM = {"\\\", \\\"threshold_high\\\":100, \\\"threshold_low\\\":20, \\\"threshold_medium\\\":50, \\\"threshold_very_high\\\":200, \\\"threshold_very_low\\\":0, \\\"unit_name\\\":\\\""};  //Use this for PM10
//Add microgram per cubic meter || fahrenheit || percent
const char data2_10 [] PROGMEM = {"\\\"}\", \"compression\":false}"};

//Data_3
const char data3 [] PROGMEM = {"\\\",\\\"timezone_offset\\\":0,\\\"milliseconds\\\":0,\\\"measured_value\\\":"};

//Time request
//const char time_request[] PROGMEM = {"HEAD / HTTP/1.1\nHost: www.aircasting.org"};
const char time_request[] PROGMEM = {"HEAD / HTTP/1.1\nHost: www.aircasting.org\nContent-Length: "};
const char time_request_1 [] PROGMEM = {"\nContent-Type: text/html\nConnection: close\n\n"};

//Tables
const char* const post_table [] PROGMEM = {post, post_1};
const char* const data_1_table [] PROGMEM = {data1, data1_1, data1_2, data1_3};
const char* const data_2_table [] PROGMEM = {data2, data2_1, data2_2, data2_3, data2_4, data2_5, data2_6, data2_7, data2_8, data2_9, data2_10};
const char* const data_3_table [] PROGMEM = {data3};
const char* const time_request_table [] PROGMEM = {time_request, time_request_1};
