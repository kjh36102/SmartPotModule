//PinSetup.h
#ifndef __PINSETUP_H__
#define __PINSETUP_H__

//-------------------------------------------------------------

#include <Arduino.h>

//-------------------------------------------------------------
#define LOGKEY "PinSetup.h"
#include "Logger.h"
//-------------------------------------------------------------

//핀 이름과 번호 정의
#define PIN_CONN_BTN 0
#define PIN_CONN_LED 15



/**
위 정의된 핀번호를 기반으로 GPIO 모드를 초기화하는 함수
*/
void initPins() {
  pinMode(PIN_CONN_BTN, INPUT_PULLUP);
  pinMode(PIN_CONN_LED, OUTPUT);

  pinMode(15, OUTPUT);

  LOGLN("All pin has been inited.");
}


//-------------------------------------------------------------
#endif  //__PINSETUP_H__