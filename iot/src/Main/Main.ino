//우선적 include
#include "PinSetup.h"
#include "NetworkSetup.h"
#include "MultitaskRTOS.h"
#include "PowerMonitor.h"
#include "SerialCommander.h"

//비우선적 include
#include "RoutersAP.h"
#include "RoutersSTA.h"
#include "TaskReadConnBtn.h"
#include "SoilUpdater.h"


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
  enableLogging("SoilUpdater.h");
}

void registerCommands() {
  SerialCommander& commander = SerialCommander::getInstance();

  commander.registerCallback("db", [](String line) {
    DB_Manager& dbManager = DB_Manager::getInstance();  // DB_Manager 인스턴스 얻기

    if (line.equals("open")) {
      dbManager.open("data");
    } else if (line.equals("close")) {
      dbManager.close();
    } else {
      if (dbManager.execute(line.c_str()) == SQLITE_OK) {  // SQL 명령 실행
        char* result = dbManager.getResult();              // 결과 받기
        LOGLN(result);                                     // 결과 출력
      } else {
        LOGLN("SQL 수행 실패");
      }
    }
  });

  commander.registerCallback("network", [](String line) {
    if (line.equals("setup")) {
      connectPhase = ConnectPhase::SETUP;
      initNetwork(true);
    }
  });

  commander.registerCallback("system", [](String line) {
    if (line.equals("free")) {
      LOGF("FreeHeapMemory: %u\n", esp_get_free_heap_size());
    }
  });

  commander.registerCallback("soilupdater", [](String line) {
    SoilUpdater& soilUpdater = SoilUpdater::getInstance();

    if (line.equals("on")) {
      soilUpdater.on();
      LOGLN("SoilUpdater 켜짐");
    } else if (line.equals("off")) {
      soilUpdater.off();
      LOGLN("SoilUpdater 꺼짐");
    }
  });
}


void setup() {
  Serial.begin(9600);
  
  enableLoggings();

  registerCommands();

  initPins();

  createAndRunTask(PowerMonitor::taskFunction, "PowerMonitor", 10000);
  createAndRunTask(SerialCommander::taskFunction, "SerialCommander", 30000);
  // createAndRunTask(tReadConnBtn, "TaskReadConnBtn", 3000);
  createAndRunTask(tControlWifiLed, "TaskControlWifiLed");
  createAndRunTask(SoilUpdater::taskFunction, "SoilUpdater", 10000, 2);

  setupAPRouters();
  setupSTARouters();

  //외부전원 차단시 DB닫기 실행함수로 등록
  PowerMonitor& powerMonitor = PowerMonitor::getInstance();

  powerMonitor.appendShutdownProcess([]() {
    DB_Manager& dbManager = DB_Manager::getInstance();
    dbManager.close();
  });
}

void loop() {
  serverSTA.handleClient();
  serverAP.handleClient();
}