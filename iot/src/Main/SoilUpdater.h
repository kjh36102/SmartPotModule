#ifndef __SOIL_UPDATER__
#define __SOIL_UPDATER__

//-------------------------------------------------------------
#include <Arduino.h>
#include "SoilSensor.h"
#include "DB_Manager.h"

#define PIN_SOIL_DERE 13
#define PIN_SOIL_RO 32
#define PIN_SOIL_DI 33

//-------------------------------------------------------------
#define LOGKEY "SoilUpdater.h"
#include "Logger.h"
//-------------------------------------------------------------

#define SOIL_UPDATE_INTERVAL 5000

class SoilUpdater {

private:
  SoilSensor soilSensor;

  static SoilUpdater* instance;

  bool stateRunning = false;

  SoilUpdater()
    : soilSensor(PIN_SOIL_DERE, PIN_SOIL_RO, PIN_SOIL_DI) {
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
    DB_Manager& dbManager = DB_Manager::getInstance();

    for (;;) {

      if (stateRunning && dbManager.isOpened()) {
        float* received = soilSensor.read();

        sprintf(sql_buffer, "UPDATE soil_data SET hm=%.1f, tm=%.1f, ec=%.0f, ph=%.1f, n=%.0f, p=%.0f, k=%.0f, lt=0",
                received[0],  //hm
                received[1],  //tm
                received[2],  //ec
                received[3],  //ph
                received[4],  //n
                received[5],  //p
                received[6]   //k
                              //여기에 광량 추가해야함
        );

        dbManager.execute(sql_buffer);
      }

      vTaskDelay(SOIL_UPDATE_INTERVAL);  //매 5초마다 실행
    }
  }

  void on() {
    stateRunning = true;
  }

  void off() {
    stateRunning = false;
  }
};

// static 멤버 변수 초기화
SoilUpdater* SoilUpdater::instance = nullptr;


//-------------------------------------------------------------
#endif  //__SOIL_UPDATER__