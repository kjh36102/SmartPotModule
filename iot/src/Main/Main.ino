//우선적 include
#include "PinSetup.h"
#include "NetworkSetup.h"
#include "MultitaskRTOS.h"
#include "PowerMonitor.h"
#include "SerialCommander.h"
// #include "TaskDBManager.h"

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
  enableLogging("PowerMonitor.h");
  // enableLogging("MultitaskRTOS.h");
  enableLogging("TaskReadConnBtn.h");
  enableLogging("SerialCommander.h");
  enableLogging("DB_Manager.h");
  // enableLogging("TaskDBManager.h");
  // enableLogging("TaskUpateSoilData.h");
}

void registerCommands() {
  SerialCommander& commander = SerialCommander::getInstance();



  commander.registerCallback("db", [](String line){
    DB_Manager dbManager(1024);  // DB_Manager 생성 (512바이트 크기의 버퍼를 사용)

    if (dbManager.open("data")) {  // 'myDatabase' 데이터베이스 열기

      dbManager.execute(line.c_str());  // SQL 명령 실행

      char* result = dbManager.getResult();  // 결과 받기

      LOGLN(result);  // 결과 출력

      dbManager.close();  // 데이터베이스 닫기
    }
  });
}

void setup() {
  Serial.begin(9600);
  enableLoggings();
  registerCommands();

  initPins();

  createAndRunTask(PowerMonitor::taskFunction, "PowerMonitor");
  createAndRunTask(SerialCommander::taskFunction, "SerialCommander", 50000);

  // createAndRunTask(tReadConnBtn, "TaskReadConnBtn", 3000);
  createAndRunTask(tControlWifiLed, "TaskControlWifiLed");
  // createAndRunTask(tMonitorExtSPwr, "TaskMonitorExtPwr", 3000);

  // createAndRunTask(tListenUpdateSoilData,"TaskListenUpdateSoilData", 5000, 2);

  setupAPRouters();
  setupSTARouters();


  // createAndRunTask(tTestDBManager, "TestDBManager", 30000);
}

void loop() {
  serverSTA.handleClient();
  serverAP.handleClient();
}