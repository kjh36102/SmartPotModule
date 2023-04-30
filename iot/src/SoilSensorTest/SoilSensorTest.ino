#include "SoilSensor.h"   //토양센서 라이브러리
#include <ArduinoJson.h>  //json 라이브러리

//토양센서 핀 정의
#define SoilSensor_DERE 12
#define SoilSensor_RO 27
#define SoilSensor_DI 13

//토양센서 객체 생성
SoilSensor soilSensor(SoilSensor_DERE, SoilSensor_RO, SoilSensor_DI);

void setup() {
  Serial.begin(9600);
}

void loop() {
  float* received = soilSensor.read();

  //json 생성
  StaticJsonDocument<200> doc;
  doc["humid"] = String(received[0], 1);  //일부 값 소수점 1째 자리까지 출력
  doc["temp"] = String(received[1], 1);
  doc["ec"] = received[2];
  doc["ph"] = String(received[3], 1);
  doc["nitro"] = received[4];
  doc["phos"] = received[5];
  doc["pota"] = received[6];

  //json 출력
  String output;
  serializeJson(doc, output);
  Serial.println(output);

  delay(2000);
}