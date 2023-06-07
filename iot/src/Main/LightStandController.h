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
  int initialDutyPercent = 80;

  LightStandController() {
    fanController = PWMController(0, 26, 22000, 600, 750);  //채널, 핀번호, 주파수, 최소듀티, 최대듀티
    lightController = PWMController(2, 27, 5000, 300, 1024);

    setUnionDuty(initialDutyPercent);

    Wire.begin();
    lightMeter.begin();
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

    int lightDuty = (lightController.getMaxDuty() - lightController.getMinDuty()) * (100 - percent) / 100 + lightController.getMinDuty();
    int fanDuty = (fanController.getMaxDuty() - fanController.getMinDuty()) * (100 - percent) / 100 + fanController.getMinDuty();

    lightController.setDutyCycle(lightDuty);
    fanController.setDutyCycle(fanDuty);
    LOGF("LightStand UnionDuty set to %d%%\n", percent);
  }

  void on() {
    lightController.start();
    fanController.start();

    DB_Manager& dbManager = DB_Manager::getInstance();

    //plant_manage에서 lightstate 업데이트하기
    if (dbManager.execute("update plant_manage set l_on=1") != SQLITE_OK) {
      LOGLN("Failed to update l_on state on");
    }
  }

  void off() {
    lightController.stop();
    fanController.stop();

    DB_Manager& dbManager = DB_Manager::getInstance();

    //plant_manage에서 lightstate 업데이트하기
    if (dbManager.execute("update plant_manage set l_on=0") != SQLITE_OK) {
      LOGLN("Failed to update l_on state off");
    }
  }

  float readLightLevel() {
    float readed = -1;

    // Serial.println("Start reading lightlevel...");
    do {
      readed = lightMeter.readLightLevel();
      vTaskDelay(1);
    } while (readed < 0);
    // Serial.println("Done reading lightlevel");

    // Serial.print("Readed lux value: ");
    // Serial.println(readed);
    // if(readed != -1)
    this->lastLuxValue = readed;
    return this->lastLuxValue;
  }
};


// 초기화
LightStandController* LightStandController::instance = nullptr;

//-------------------------------------------------------------
#endif  //__LIGHT_STAND_CONTROLLER_H__