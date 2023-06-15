#ifndef __COMMAND_BUTTON_MANAGER_H__
#define __COMMAND_BUTTON_MANAGER_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "PinSetup.h"
#include "NetworkSetup.h"
#include "esp_system.h"
#include "MultitaskRTOS.h"

//-------------------------------------------------------------
#define LOGKEY "CommandButtonManager.h"
#include "Logger.h"
//-------------------------------------------------------------

#define PIN_CONN_BTN 0
#define PIN_CONN_LED 15

class CommandButtonManager {
private:

  static CommandButtonManager* instance;

  static bool buttonState;

  CommandButtonManager() {
    pinMode(PIN_CONN_BTN, INPUT_PULLUP);
    pinMode(PIN_CONN_LED, OUTPUT);
  }

public:

  static CommandButtonManager& getInstance() {
    if (instance == nullptr) {
      instance = new CommandButtonManager();
      createAndRunTask(tReadConnBtn, "TaskReadConnBtn", 6000);
      createAndRunTask(tControlWifiLed, "TaskControlWifiLed");
    }

    return *instance;
  }

  static void blinkLedNtime(unsigned int nTime, unsigned int interval = 100, unsigned int delay = 1000) {
    unsigned int i = 0;
    for (; i < nTime; i++) {
      digitalWrite(PIN_CONN_LED, HIGH);
      vTaskDelay(interval);
      digitalWrite(PIN_CONN_LED, LOW);
      vTaskDelay(interval);
    }

    vTaskDelay(delay);
  }


  static void tReadConnBtn(void* taskParams) {
    unsigned long buttonPressStartTime = 0;

    for (;;) {
      bool reading = !digitalRead(PIN_CONN_BTN);

      if (reading && !buttonState) {
        // 버튼이 눌린 순간
        buttonState = true;
        buttonPressStartTime = millis();
      } else if (!reading && buttonState) {
        // 버튼이 놓인 순간
        buttonState = false;

        unsigned long buttonPressDuration = millis() - buttonPressStartTime;

        LOGF("Button press total time: %d\n", buttonPressDuration);
        if (buttonPressDuration >= 3000 && buttonPressDuration < 6000) {
          // 버튼을 3초 이상 누른 경우
          connectPhase = ConnectPhase::SETUP;
          initNetwork(true);
        } else if (buttonPressDuration >= 6000) {
          // 버튼을 6초 이상 누른 경우
          DB_Manager& dbManager = DB_Manager::getInstance();
          dbManager.dropAllTables();
          esp_restart();
        }
      }

      if (buttonState) {
        digitalWrite(PIN_CONN_LED, HIGH);
        vTaskDelay(500);
        digitalWrite(PIN_CONN_LED, LOW);
      }

      vTaskDelay(500);
    }
  }

  static void tControlWifiLed(void* taskParams) {

    for (;;) {

      if (buttonState) {
        vTaskDelay(500);
        continue;
      }

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
        case ConnectPhase::RECONNECT:
          blinkLedNtime(4);
          break;
        case ConnectPhase::COMPLETE:
          blinkLedNtime(5);
          connectPhase = ConnectPhase::IDLE;
          break;
        case ConnectPhase::IDLE:
          digitalWrite(PIN_CONN_LED, HIGH);
          break;
      }

      vTaskDelay(1);
    }
  }
};

CommandButtonManager* CommandButtonManager::instance = nullptr;
bool CommandButtonManager::buttonState = false;



//-------------------------------------------------------------
#endif  //__COMMAND_BUTTON_MANAGER_H__