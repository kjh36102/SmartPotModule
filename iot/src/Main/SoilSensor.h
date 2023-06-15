#ifndef __SOIL_SENSOR_H__
#define __SOIL_SENSOR_H__

#include <SoftwareSerial.h>  //<- EspSoftwareSerial.h
#include <Wire.h>
#include <vector>
#include "MultitaskRTOS.h"

#define PIN_SOIL_DERE 13
#define PIN_SOIL_RO 32
#define PIN_SOIL_DI 33

class SoilSensor {
private:
  const byte CODE[8] = { 0x01, 0x03, 0x00, 0x00, 0x00, 0x07, 0x04, 0x08 };
  byte buffer[19] = {
    0,
  };
  float receive[7] = {
    0.0,
  };
  EspSoftwareSerial::UART soilSerial;

  static portMUX_TYPE criticalMutex;
  static SemaphoreHandle_t xMutex;

  static SoilSensor* instance;

  SoilSensor() {
    this->soilSerial.begin(4800, SWSERIAL_8N1, PIN_SOIL_RO, PIN_SOIL_DI, false);

    pinMode(PIN_SOIL_DERE, OUTPUT);
    digitalWrite(PIN_SOIL_DERE, LOW);
  }

public:
  static SoilSensor& getInstance(){
    if(instance == nullptr){
      instance = new SoilSensor();
    }

    return *instance;
  }

  std::vector<float> read() {
    std::vector<float> ret(7);

    xSemaphoreTake(xMutex, portMAX_DELAY);  // 뮤텍스 획득
    taskENTER_CRITICAL(&criticalMutex);     //다른 FreeRTOS 태스크의 선점방지, 크리티컬 영역 실행중에는 다른 태스크가 실행되지않음, 최대한 짧게 유지하기 위해 이곳에 작성함


    digitalWrite(PIN_SOIL_DERE, HIGH);
    this->soilSerial.write(this->CODE, 8);

    vTaskDelay(100);
    digitalWrite(PIN_SOIL_DERE, LOW);

    vTaskDelay(100);

    for (byte i = 0; i < 19; i++) {
      this->buffer[i] = this->soilSerial.read();  //값 읽기
    }

    taskEXIT_CRITICAL(&criticalMutex);  //크리티컬 영역 종료
    xSemaphoreGive(xMutex);             // 뮤텍스 획득


    for (byte i = 0; i < 7; i++) {
      ret[i] = this->concat_byte(buffer, 3 + 2 * i);
    }

    ret[0] /= 10;
    ret[1] /= 10;
    ret[3] /= 10;

    return ret;
  }

  uint16_t concat_byte(uint8_t* buff, uint8_t startIdx) {
    return (((uint16_t)(buff[startIdx] << 8) | (buff[startIdx + 1])));
  }
};

SoilSensor* SoilSensor::instance = nullptr;
portMUX_TYPE SoilSensor::criticalMutex = portMUX_INITIALIZER_UNLOCKED;
SemaphoreHandle_t SoilSensor::xMutex = xSemaphoreCreateMutex();


#endif  // __SOIL_SENSOR_H__