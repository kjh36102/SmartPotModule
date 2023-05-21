//우선적 include
#include "PinSetup.h"
#include "MultitaskRTOS.h"
#include "NetworkSetup.h"

//비우선적 include
#include "RoutersAP.h"
#include "RoutersSTA.h"
#include "TaskReadConnBtn.h"

//-------------------------------------------------------------
#define LOGKEY "Main.h"
#include "Logger.h"
//-------------------------------------------------------------

void enableLoggings(){
  enableLogging("Main.h");
  enableLogging("PinSetup.h");
  enableLogging("NetworkSetup.h");
  enableLogging("RoutersAP.h");
  enableLogging("RoutersSTA.h");
  enableLogging("UDPLibrary.h");
  // enableLogging("MultitaskRTOS.h");

}

void setup() {
  Serial.begin(9600);
  enableLoggings();

  initPins();

  createAndRunTask(tReadConnBtn, "TaskReadConnBtn", 3000);
  createAndRunTask(tControlWifiLed, "TaskControlWifiLed", 1000);

  setupAPRouters();
  setupSTARouters();

  initNetwork();
}

void loop() {
  serverSTA.handleClient();
  serverAP.handleClient();
}