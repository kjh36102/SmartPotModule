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
#include "TaskUpdateSoilData.h"


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
  // enableLogging("TaskUpateSoilData.h");
}

void setup() {
  Serial.begin(9600);
  enableLoggings();

  initPins();

  // createAndRunTask(tReadConnBtn, "TaskReadConnBtn", 3000);
  createAndRunTask(tControlWifiLed, "TaskControlWifiLed");
  createAndRunTask(tMonitorExtPwr, "TaskMonitorExtPwr", 3000);

  // createAndRunTask(tListenUpdateSoilData,"TaskListenUpdateSoilData", 5000, 2);

  setupAPRouters();
  setupSTARouters();


  createAndRunTask(tTestDBManager, "TestDBManager", 30000);
}

void loop() {
  serverSTA.handleClient();
  serverAP.handleClient();

}