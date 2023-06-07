#ifndef __SOIL_UPDATER__
#define __SOIL_UPDATER__

//-------------------------------------------------------------
#include <Arduino.h>
#include "SoilSensor.h"
#include "DB_Manager.h"
#include "LightStandController.h"
#include "TimeUpdater.h"

#define PIN_SOIL_DERE 13
#define PIN_SOIL_RO 32
#define PIN_SOIL_DI 33

//-------------------------------------------------------------
#define LOGKEY "SoilUpdater.h"
#include "Logger.h"
//-------------------------------------------------------------

#define SOIL_UPDATE_INTERVAL 9000//00  //15분

class SoilUpdater {

private:
  SoilSensor soilSensor;

  static SoilUpdater* instance;

  bool stateRunning = false;

  SemaphoreHandle_t mutex;

  SoilUpdater()
    : soilSensor(PIN_SOIL_DERE, PIN_SOIL_RO, PIN_SOIL_DI) {
    mutex = xSemaphoreCreateMutex();  // 뮤텍스 생성
  }

public:
  // 다른 복사 생성자와 할당 연산자를 삭제
  SoilUpdater(const SoilUpdater&) = delete;
  SoilUpdater& operator=(const SoilUpdater&) = delete;

  static SoilUpdater& getInstance() {
    if (instance == nullptr) {
      instance = new SoilUpdater();
    }
    return *instance;
  }

  char sql_buffer[100] = {
    //update SQL문을 담을 버퍼
    '\0',
  };

  static void taskFunction(void* pvParameters) {
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
    // xSemaphoreTake(mutex, portMAX_DELAY);  // 뮤텍스 획득

    bool ret = false;

    DB_Manager& dbManager = DB_Manager::getInstance();

    if ((stateRunning || force) && dbManager.isOpened()) {
      LightStandController& lightStand = LightStandController::getInstance();
      TimeUpdater& timeUpdater = TimeUpdater::getInstance();

      float* received;

      do {
        received = soilSensor.read();
      } while (!checkValidValue(received));  //값이 올바르지않으면 다시시도

      sprintf(sql_buffer, "UPDATE soil_data SET hm=%.1f, tm=%.1f, ec=%.0f, ph=%.1f, n=%.0f, p=%.0f, k=%.0f, lt=%.0f, ts='%s'",
              received[0],                  //hm
              received[1],                  //tm
              received[2],                  //ec
              received[3],                  //ph
              received[4],                  //n
              received[5],                  //p
              received[6],                  //k
              lightStand.readLightLevel(),  //lux
              timeUpdater.getCurrentTime().c_str()

      );

      dbManager.execute(sql_buffer);
      ret = true;
    }

    xSemaphoreGive(mutex);  // 뮤텍스 반환
    return ret;
  }

  bool checkValidValue(float* received) {
    if (received == nullptr) {
      LOGLN("checkValidValue에서 nullptr받음");
      vTaskDelay(5000);
      return false;
    }

    if (
      (received[0] < 0 || received[0] > 100) || (received[1] < -40 || received[1] > 60) || (received[2] < 0 || received[2] > 2000) || (received[3] < 0 || received[3] > 10) || (received[4] < 0 || received[4] > 2000) || (received[5] < 0 || received[5] > 2000) || (received[6] < 0 || received[6] > 2000)) {
      LOGLN("SoilSensor Value is not valid! Retrying..");
      vTaskDelay(100);  //올바르지않으면 잠시 대기
      return false;
    }

    return true;
  }
};

// static 멤버 변수 초기화
SoilUpdater* SoilUpdater::instance = nullptr;


//-------------------------------------------------------------
#endif  //__SOIL_UPDATER__