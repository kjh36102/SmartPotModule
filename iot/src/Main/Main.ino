
//ESP include
#include "esp_system.h"

//우선적 include
#include "PinSetup.h"
#include "NetworkSetup.h"
#include "MultitaskRTOS.h"
#include "PowerMonitor.h"
#include "SerialCommander.h"

//비우선적 include
#include "RoutersAP.h"
#include "RoutersSTA.h"
#include "CommandButtonManager.h"
#include "SoilUpdater.h"
#include "PWMController.h"
#include "LightStandController.h"
#include "TimeUpdater.h"
#include "NextOperationHandler.h"
#include "WaterJarController.h"
#include "SystemFanController.h"


//-------------------------------------------------------------
#define LOGKEY "Main.h"
#include "Logger.h"
//-------------------------------------------------------------

void enableLoggings() {
  // Serial.begin(9600);
  // enableLogging("Main.h");
  // enableLogging("PinSetup.h");
  // enableLogging("NetworkSetup.h");
  // enableLogging("RoutersAP.h");
  // enableLogging("RoutersSTA.h");
  // enableLogging("UDPLibrary.h");
  // enableLogging("PowerMonitor.h");
  // enableLogging("MultitaskRTOS.h");
  // enableLogging("CommandButtonManager.h");
  // enableLogging("SerialCommander.h");
  // enableLogging("DB_Manager.h");
  // enableLogging("SoilUpdater.h");
  // enableLogging("PWMController.h");
  // enableLogging("NextOperationHandler.h");
  // enableLogging("TimeUpdater.h");
  // enableLogging("WaterJarController.h");
}

void initSingletons() {
  PowerMonitor::getInstance();
  SystemFanController::getInstance();
  DB_Manager::getInstance();
  CommandButtonManager::getInstance();
  // SerialCommander::getInstance();
  LightStandController::getInstance();  //객체 생성해줘야 팬 꺼짐
  WaterJarController::getInstance();
  NextOperationHandler::getInstance();
  SoilUpdater::getInstance();
}

void setup() {
  enableLoggings();
  initSingletons();

  delay(1000);
  initNetwork();

  setupAPRouters();
  setupSTARouters();
}

void loop() {
  serverSTA.handleClient();
  serverAP.handleClient();
}