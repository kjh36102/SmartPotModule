#ifndef __LIGHT_STAND_CONTROLLER_H__
#define __LIGHT_STAND_CONTROLLER_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "PWMController.h"

//-------------------------------------------------------------
#define LOGKEY "LightStandController.h"
#include "Logger.h"
//-------------------------------------------------------------

class LightStandController {
private:
  static LightStandController* instance;
  PWMController fanController;
  PWMController lightController;

  LightStandController()
    : fanController(0, 26, 20, 600, 750), lightController(2, 27, 5000, 300, 1024) {
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
    int lightDuty = (lightController.getMaxDuty() - lightController.getMinDuty()) * (100 - percent) / 100 + lightController.getMinDuty();
    int fanDuty = (fanController.getMaxDuty() - fanController.getMinDuty()) * (100 - percent) / 100 + fanController.getMinDuty();

    lightController.setDutyCycle(lightDuty);
    fanController.setDutyCycle(fanDuty);
    LOGF("LightStand UnionDuty set to %d%%\n", percent);
  }

  void on(){
    lightController.start();
    fanController.start();
  }

  void off(){
    lightController.stop();
    fanController.stop();
  }
};


// 초기화
LightStandController* LightStandController::instance = nullptr;

//-------------------------------------------------------------
#endif  //__LIGHT_STAND_CONTROLLER_H__