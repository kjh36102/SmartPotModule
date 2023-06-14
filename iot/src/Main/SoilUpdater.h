#ifndef __SOIL_UPDATER__
#define __SOIL_UPDATER__

//-------------------------------------------------------------
#include <Arduino.h>
#include "SoilSensor.h"
#include "DB_Manager.h"
#include "LightStandController.h"
#include "TimeUpdater.h"



//-------------------------------------------------------------
#define LOGKEY "SoilUpdater.h"
#include "Logger.h"
//-------------------------------------------------------------

#define SOIL_UPDATE_INTERVAL 900000  //15분

class SoilUpdater {

private:

  static SoilUpdater* instance;

  bool stateRunning = false;

  SoilSensor* soilSensor;
  LightStandController* lightStand;
  TimeUpdater* timeUpdater;

  char sql_buffer[100];



  SoilUpdater() {
    soilSensor = &SoilSensor::getInstance();
    lightStand = &LightStandController::getInstance();
    timeUpdater = &TimeUpdater::getInstance();
  }

public:
  // 다른 복사 생성자와 할당 연산자를 삭제
  SoilUpdater(const SoilUpdater&) = delete;
  SoilUpdater& operator=(const SoilUpdater&) = delete;

  float savedValue[8] = {
    0,
  };

  static SoilUpdater& getInstance() {
    if (instance == nullptr) {
      instance = new SoilUpdater();

      createAndRunTask(SoilUpdater::taskFunction, "SoilUpdater", 10000, 2);
      // createAndRunTask(SoilUpdater::taskMonitorSensorValue, "taskMonitorSensorValue", 6000, 2);
    }
    return *instance;
  }

  // static void taskMonitorSensorValue(void* taskParams) {
  //   SoilUpdater& soilUpdater = SoilUpdater::getInstance();
  //   char buffer[100];

  //   for (;;) {
  //     // float* received = soilUpdater.soilSensor->read();
  //     std::vector<float> received = soilUpdater.soilSensor->read();

  //     // for (byte i = 0; i < 8; i++) {
  //     //   soilUpdater.savedValue[i] = received[i];
  //     // }

  //     sprintf(buffer, "[soil_data] hm:%.1f, tm:%.1f, ec:%.0f, ph:%.1f, n:%.0f, p:%.0f, k:%.0f, lt:%.0f, ts:%s",
  //             received[0],                               //hm
  //             received[1],                               //tm
  //             received[2],                               //ec
  //             received[3],                               //ph
  //             received[4],                               //n
  //             received[5],                               //p
  //             received[6],                               //k
  //             soilUpdater.lightStand->readLightLevel(),  //lux
  //             soilUpdater.timeUpdater->getCurrentTime().c_str()

  //     );

  //     LOGLN(buffer);
  //     vTaskDelay(500);
  //   }
  // }

  static void taskFunction(void* taskParams) {
    SoilUpdater::getInstance().run();
  }

  void run() {
    // Get the current tick count
    TickType_t xLastWakeTime = xTaskGetTickCount();

    // Define the delay period in ticks (1000 ticks per second, so 60000 for one minute)
    const TickType_t xDelay = SOIL_UPDATE_INTERVAL / portTICK_PERIOD_MS;

    SoilUpdater& soilupdater = SoilUpdater::getInstance();

    for (;;) {
      soilupdater.measureNow();

      // Delay until the next period
      vTaskDelayUntil(&xLastWakeTime, xDelay);
    }
  }

  void on() {
    stateRunning = true;
  }

  void off() {
    stateRunning = false;
  }

  bool measureNow(bool force = false) {
    bool ret = false;

    DB_Manager& dbManager = DB_Manager::getInstance();

    if ((stateRunning || force) && dbManager.isOpened()) {

      // float* received;
      std::vector<float> received;

      do {
        received = soilSensor->read();
      } while (!checkValidValue(received));  //값이 올바르지않으면 다시시도

      sprintf(sql_buffer, "UPDATE soil_data SET hm=%.1f, tm=%.1f, ec=%.0f, ph=%.1f, n=%.0f, p=%.0f, k=%.0f, lt=%.0f, ts='%s'",
              received[0],                   //hm
              received[1],                   //tm
              received[2],                   //ec
              received[3],                   //ph
              received[4],                   //n
              received[5],                   //p
              received[6],                   //k
              lightStand->readLightLevel(),  //lux
              timeUpdater->getCurrentTime().c_str()

      );

      dbManager.execute(sql_buffer);
      ret = true;
    }

    return ret;
  }

  // bool checkValidValue(float* received) {
  bool checkValidValue(std::vector<float> received) {
    // if (received == nullptr) {
    if (received.empty()) {
      LOGLN("checkValidValue에서 nullptr받음");
      vTaskDelay(5000);
      return false;
    }

    if (
      (received[0] < 0 || received[0] > 100) || (received[1] < -40 || received[1] > 60) || (received[2] < 0 || received[2] > 2000) || (received[3] < 0 || received[3] > 10) || (received[4] < 0 || received[4] > 2000) || (received[5] < 0 || received[5] > 2000) || (received[6] < 0 || received[6] > 2000)) {
      LOGLN("SoilSensor Value is not valid! Retrying..");
      vTaskDelay(250);  //올바르지않으면 잠시 대기
      return false;
    }

    return true;
  }

  std::vector<float> readUntilSuccess() {
    std::vector<float> received;
    do {
      received = soilSensor->read();
    } while (!checkValidValue(received));  //값이 올바르지않으면 다시시도

    return received;
  }
};

// static 멤버 변수 초기화
SoilUpdater* SoilUpdater::instance = nullptr;


//-------------------------------------------------------------
#endif  //__SOIL_UPDATER__