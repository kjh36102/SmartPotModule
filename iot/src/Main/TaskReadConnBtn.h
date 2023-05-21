#ifndef __TASK_READ_CONN_BTN__
#define __TASK_READ_CONN_BTN__

//-------------------------------------------------------------
#include <Arduino.h>
#include "PinSetup.h"
#include "NetworkSetup.h"

//-------------------------------------------------------------
#define LOGKEY "TaskReadConnBtn.h"
#include "Logger.h"
//-------------------------------------------------------------

void blinkLedNtime(unsigned int nTime, unsigned int interval = 100, unsigned int delay = 1000) {
  unsigned int i = 0;
  for (; i < nTime; i++) {
    digitalWrite(PIN_CONN_LED, HIGH);
    vTaskDelay(interval);
    digitalWrite(PIN_CONN_LED, LOW);
    vTaskDelay(interval);
  }

  vTaskDelay(delay);
}

void tReadConnBtn(void* taskParams) {
  for (;;) {
    bool reading = !digitalRead(PIN_CONN_BTN);

    if (reading) {
      unsigned long pushedTime = millis();

      while (true) {
        reading = !digitalRead(PIN_CONN_BTN);
        if (!reading) break;

        if (millis() - pushedTime >= 5000) {
          connectPhase = ConnectPhase::SETUP;
          initNetwork(true);
          break;
        }
        vTaskDelay(500);
      }
    }

    vTaskDelay(1000);
  }
}

void tControlWifiLed(void* taskParams) {
  for (;;) {

    switch (connectPhase) {
      case ConnectPhase::INITIAL:
        digitalWrite(PIN_CONN_LED, LOW);
        break;
      case ConnectPhase::SETUP:
      case ConnectPhase::STA_INFO:
      case ConnectPhase::STA_CONNECTED:
        blinkLedNtime(1);
        break;
      case ConnectPhase::UDP_BROADCAST:
        blinkLedNtime(2);
        break;
      case ConnectPhase::UDP_ACK:
        blinkLedNtime(3);
        break;
      case ConnectPhase::COMPLETE:
        blinkLedNtime(5);
        connectPhase = ConnectPhase::IDLE;
        break;
      case ConnectPhase::IDLE:
        digitalWrite(PIN_CONN_LED, HIGH);
        break;
      case ConnectPhase::REFRESH:
        blinkLedNtime(1, 100, 0);
    }

    vTaskDelay(1);
  }
}


//-------------------------------------------------------------
#endif  //__TASK_READ_CONN_BTN__