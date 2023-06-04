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
#include "PWMController.h"
#include "LightStandController.h"


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
  enableLogging("PWMController.h");
}


void registerCommands() {

  SerialCommander& commander = SerialCommander::getInstance();

  commander.registerCallback("lightstand", [](String line) {
    LightStandController& lightStandCtrl = LightStandController::getInstance();
    if(line.equals("on")){
      lightStandCtrl.on();
    }else if(line.equals("off")){
      lightStandCtrl.off();
    }else{
      lightStandCtrl.setUnionDuty(line.toInt());
    }
  });

  commander.registerCallback("db", [](String line) {
    DB_Manager& dbManager = DB_Manager::getInstance();  // DB_Manager 인스턴스 얻기

    if (line.equals("open")) {
      dbManager.open("data");
    } else if (line.equals("close")) {
      dbManager.close();
    }

    else if (line.equals("createtables")) {
      if(!dbManager.isOpened()){
        LOGLN("DB가 열려있지 않아 createtables명령 수행 불가능");
        return;
      }

      dbManager.prepareTable("soil_data",
                             F("CREATE TABLE IF NOT EXISTS soil_data (id INTEGER PRIMARY KEY AUTOINCREMENT, tm REAL, hm REAL, n REAL, p REAL, k REAL, ph REAL, ec INTEGER, lt INTEGER, ts TEXT DEFAULT (datetime('now','localtime')))"),
                             F("INSERT INTO soil_data(id,tm,hm,n,p,k,ph,ec,lt) VALUES (0,0,0,0,0,0,0,0,0)"));

      dbManager.prepareTable("wifi_info",
                             F("CREATE TABLE IF NOT EXISTS wifi_info (id INTEGER PRIMARY KEY, ssid_ap TEXT, pw_ap TEXT, ssid_sta TEXT, pw_sta TEXT)"),
                             F("INSERT INTO wifi_info VALUES(0, 'SmartPotModule', '', '', '')"));

      dbManager.prepareTable("plant_manage",
                             F("CREATE TABLE IF NOT EXISTS plant_manage (id INTEGER PRIMARY KEY, w_auto INTEGER, l_auto INTEGER, w_on INTEGER, l_on INTEGER, ot INTEGER)"),
                             F("INSERT INTO plant_manage VALUES(0, 0, 0, 0, 0, 0)"));

      dbManager.prepareTable("manage_auto",
                             F("CREATE TABLE IF NOT EXISTS manage_auto (id INTEGER PRIMARY KEY, hm REAL, th REAL, lt INTEGER, dr INTEGER)"),
                             F("INSERT INTO manage_auto VALUES(0, 0, 0, 0, 0)"));

      dbManager.prepareTable("manage_water",
                             F("CREATE TABLE IF NOT EXISTS manage_water (id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, wt INTEGER, no TEXT)"),
                             NULL,
                             false);

      dbManager.prepareTable("manage_light",
                             F("CREATE TABLE IF NOT EXISTS manage_light (id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, ls INTEGER, no TEXT)"),
                             NULL,
                             false);

    } else if (line.equals("droptables")) {
      dbManager.dropAllTables();
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

void initFirstPriority(){
  LightStandController::getInstance();  //객체 생성해줘야 팬 꺼짐
}

void setup() {
  Serial.begin(9600);
  initFirstPriority();
  enableLoggings();
  registerCommands();
  initPins();

  createAndRunTask(PowerMonitor::taskFunction, "PowerMonitor", 10000);
  createAndRunTask(SerialCommander::taskFunction, "SerialCommander", 40000);
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