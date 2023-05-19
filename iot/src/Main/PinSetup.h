//PinSetup.h
#ifndef __PINSETUP_H__
#define __PINSETUP_H__

//-------------------------------------------

#include <Arduino.h>

//--------------------------------
#define LOGKEY "PinSetup.h"
#include "Logger.h"
//--------------------------------

//핀 이름과 번호 정의
#define PIN_CONNECT_BTN 18
#define PIN_CONNECT_LED 19

/**
위 정의된 핀번호를 기반으로 GPIO 모드를 초기화하는 함수
*/
void initPins() {
  pinMode(PIN_CONNECT_BTN, INPUT);
  pinMode(PIN_CONNECT_LED, OUTPUT);


  LOGLN("All pin has been inited.");
}


//-------------------------------------------
#endif  //__PINSETUP_H__