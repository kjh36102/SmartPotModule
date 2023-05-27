//우선적 include
#include "PinSetup.h"
#include "NetworkSetup.h"
#include "MultitaskRTOS.h"
#include "TaskMonitorExtPwr.h"
#include "TaskDBManager.h"

//비우선적 include
#include "RoutersAP.h"
#include "RoutersSTA.h"
#include "TaskReadConnBtn.h"


//-------------------------------------------------------------
#define LOGKEY "Main.h"
#include "Logger.h"
//-------------------------------------------------------------

void enableLoggings() {
  enableLogging("Main.h");
  enableLogging("PinSetup.h");
  enableLogging("NetworkSetup.h");
  enableLogging("RoutersAP.h");
  enableLogging("RoutersSTA.h");
  enableLogging("UDPLibrary.h");
  // enableLogging("MultitaskRTOS.h");
  enableLogging("TaskReadConnBtn.h");
  enableLogging("TaskDBManager.h");
}

void setup() {
  Serial.begin(9600);
  enableLoggings();

  initPins();

  // createAndRunTask(tReadConnBtn, "TaskReadConnBtn", 3000);
  createAndRunTask(tControlWifiLed, "TaskControlWifiLed", 2000);
  createAndRunTask(tMonitorExtPwr, "TaskMonitorExtPwr", 3000);
  // createAndRunTask(tBroadcastingUDP, "TaskBroadcastingUDP", 2000);

  setupAPRouters();
  setupSTARouters();

  // connectPhase = ConnectPhase::SETUP;  //실행시 바로 셋업모드로(버튼없으면 사용)
  // initNetwork();

  appendShutdownProcess([]() {  //종료시 실행하는 함수 등록
    for (byte i = 0; i < 30; i++) {
      digitalWrite(PIN_CONN_LED, 1);
      delay(50);
      digitalWrite(PIN_CONN_LED, 0);
      delay(50);
    }
  });

  createAndRunTask(tTestDBManager, "TestDBManager", 10000);
}

void loop() {
  serverSTA.handleClient();
  serverAP.handleClient();
}