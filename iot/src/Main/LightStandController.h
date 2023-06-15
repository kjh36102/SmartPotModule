#ifndef __LIGHT_STAND_CONTROLLER_H__
#define __LIGHT_STAND_CONTROLLER_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "PWMController.h"
#include <Wire.h>
#include <BH1750.h>

//-------------------------------------------------------------
#define LOGKEY "LightStandController.h"
#include "Logger.h"
//-------------------------------------------------------------

class LightStandController {
private:
  static LightStandController* instance;
  PWMController fanController;
  PWMController lightController;
  BH1750 lightMeter;
  float lastLuxValue = 0;

  LightStandController() {
    fanController = PWMController(0, 26, 22000, 600, 750);  //채널, 핀번호, 주파수, 최소듀티, 최대듀티
    lightController = PWMController(2, 27, 5000, 300, 1024);

    DB_Manager& dbManager = DB_Manager::getInstance();
    dbManager.execute("select ld from manage_auto");
    JsonObject result = dbManager.getRowFromJsonArray(dbManager.getResult(), 0);

    int ld = result["ld"];
    setUnionDuty(ld);

    if (Wire.begin()) {
      if (lightMeter.begin()) {
        LOGLN("LightMeter Instance Created.");
      } else LOGLN("Failed to begin LightMeter.");
    } else {
      LOGLN("Failed to begin Wire.");
    }

    LOGLN("LightStandController Instance Created.");
  }

public:

  static LightStandController& getInstance() {
    if (instance == nullptr) {
      instance = new LightStandController();
    }

    return *instance;
  }

  void setUnionDuty(int percent) {
    if (percent < 0 || percent > 100) return;

    lightController.setPercentDuty(percent);
    fanController.setPercentDuty(percent);

    LOGF("LightStand UnionDuty set to %d%%\n", percent);
  }

  //켤때 manage_auto db에서 duty값 가져와서 켜기, 시스템팬도 마찬가지
  void on() {
    DB_Manager& dbManager = DB_Manager::getInstance();

    //plant_manage에서 lightstate 업데이트하기
    if (dbManager.execute("update plant_manage set l_on=1") != SQLITE_OK) {
      LOGLN("Failed to update l_on state on");
    }

    lightController.start();
    fanController.start();
  }

  void off() {
    DB_Manager& dbManager = DB_Manager::getInstance();

    //plant_manage에서 lightstate 업데이트하기
    if (dbManager.execute("update plant_manage set l_on=0") != SQLITE_OK) {
      LOGLN("Failed to update l_on state off");
    }

    lightController.stop();
    fanController.stop();
  }

  float readLightLevel() {
    float readed = -1;

    do {
      readed = lightMeter.readLightLevel();
      vTaskDelay(1);
    } while (readed < 0);

    this->lastLuxValue = readed;
    return this->lastLuxValue;
  }
};

// 초기화
LightStandController* LightStandController::instance = nullptr;

//-------------------------------------------------------------
#endif  //__LIGHT_STAND_CONTROLLER_H__