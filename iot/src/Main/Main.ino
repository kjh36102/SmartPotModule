/*  TODO

- 시스템쿨링세기 조절을 토양센서에서 측정한 온도값을 기반으로 변경해보기(한다면 흙속 온도는 외기보다 차가운것을 감안해야함)
- 조명 자동모드 구현하기
- 급수 자동모드 구현하기
- 1분마다 실행되는 수동모드 레코드 순회 DB 질의하면 Guru Meditation Erro뜨는문제? (간헐적이라 확실치않음)
- 물이없거나 커넥터가 연결되어있지 않을 때 tSimulateWaterLoad에서 stack canary watchpoint triggered 라고 뜸
*/

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
  enableLogging("Main.h");
  enableLogging("PinSetup.h");
  enableLogging("NetworkSetup.h");
  enableLogging("RoutersAP.h");
  enableLogging("RoutersSTA.h");
  enableLogging("UDPLibrary.h");
  enableLogging("PowerMonitor.h");
  // enableLogging("MultitaskRTOS.h");
  enableLogging("CommandButtonManager.h");
  enableLogging("SerialCommander.h");
  enableLogging("DB_Manager.h");
  enableLogging("SoilUpdater.h");
  enableLogging("PWMController.h");
  enableLogging("NextOperationHandler.h");
  enableLogging("TimeUpdater.h");
  enableLogging("WaterJarController.h");
}

void initSingletons() {
  PowerMonitor::getInstance();
  SystemFanController::getInstance();
  DB_Manager::getInstance();
  CommandButtonManager::getInstance();
  SerialCommander::getInstance();
  LightStandController::getInstance();  //객체 생성해줘야 팬 꺼짐
  WaterJarController::getInstance();
  NextOperationHandler::getInstance();
  SoilUpdater::getInstance();
}

void setup() {
  Serial.begin(9600);
  initSingletons();
  enableLoggings();

  initNetwork();

  setupAPRouters();
  setupSTARouters();
}

void loop() {
  serverSTA.handleClient();
  serverAP.handleClient();
}