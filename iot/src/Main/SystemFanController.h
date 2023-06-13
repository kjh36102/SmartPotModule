#ifndef __SYSTEM_FAN_CONTROLLER__
#define __SYSTEM_FAN_CONTROLLER__

//-------------------------------------------------------------
#include <Arduino.h>
#include "PWMController.h"
#include "DB_Manager.h"

//-------------------------------------------------------------
#define LOGKEY "SystemFanController.h"
#include "Logger.h"
//-------------------------------------------------------------

class SystemFanController : public PWMController {
private:
  static SystemFanController* instance;

  SystemFanController(int channel, int pin, double frequency, int maxDuty, int minDuty, int resolution = LEDC_TIMER_10_BIT)
    : PWMController(channel, pin, frequency, maxDuty, minDuty, resolution) {

      DB_Manager& dbManager = DB_Manager::getInstance();
      dbManager.execute("select cd from manage_auto");
      JsonObject result = dbManager.getRowFromJsonArray(dbManager.getResult(), 0);

      int cd = result["cd"];

      setPercentDuty(cd);
      start();
    }

public:
  static SystemFanController& getInstance() {
    if (instance == nullptr) {
      instance = new SystemFanController(4, 25, 22000, 300, 1024);
    }
    return *instance;
  }

};

// Initialize the static instance variable
SystemFanController* SystemFanController::instance = nullptr;

//-------------------------------------------------------------
#endif  //__SYSTEM_FAN_CONTROLLER__
