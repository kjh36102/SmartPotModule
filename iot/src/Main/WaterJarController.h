#ifndef __WATER_JAR_CONTROLLER_H__
#define __WATER_JAR_CONTROLLER_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "MultitaskRTOS.h"
#include "DB_Manager.h"

//-------------------------------------------------------------
#define LOGKEY "WaterJarController.h"
#include "Logger.h"
//-------------------------------------------------------------

#define PIN_WATERJAR_RELAY_SIGNAL 2
#define PIN_WATERJAR_READ_WATERLEVEL 34

class WaterJarController {
private:
  static WaterJarController* instance;  // 싱글톤 객체의 인스턴스를 저장할 정적 변수
  WaterJarController() {
    pinMode(PIN_WATERJAR_RELAY_SIGNAL, OUTPUT);
    pinMode(PIN_WATERJAR_READ_WATERLEVEL, INPUT);
  }

  int maxFeedTime = 30000;
  int waterLoadTime = 500;
  int waterLoadTimer = 0;
  bool waterPumpOn = false;
  int unitTime = 100;

public:
  static WaterJarController& getInstance() {
    if (instance == nullptr) {
      instance = new WaterJarController();  // 인스턴스 생성
      createAndRunTask(tOffWaterPump, "TaskOffWaterPump", 4000);
      createAndRunTask(tSimulateWaterLoad, "tSimulateWaterLoad", 3000);
    }
    return *instance;
  }

  // 다른 멤버 함수들 정의

  bool feed(int ot) {
    if (waterPumpOn) return false;
    else if (ot < 1000 || ot > maxFeedTime) {
      LOGLN("TaskFeedWater operationTIme range is invalid!");
      return false;
    }


    int* pOt = new int(ot);
    createAndRunTask(tFeedWater, "TaskFeedWater", 4000, 1, pOt);
    return true;
  }

  static void tOffWaterPump(void* taskParams) {
    WaterJarController& waterJar = WaterJarController::getInstance();

    waterJar.on();
    vTaskDelay(50);
    waterJar.off();

    vTaskDelete(NULL);
  }

  static void tSimulateWaterLoad(void* taskParams) {
    WaterJarController& waterJar = WaterJarController::getInstance();

    for (;;) {

      while (waterJar.waterPumpOn) {
        if (!waterJar.readWaterLevel()) {
          waterJar.off();
          waterJar.onWaterJarEmpty();
          break;
        }

        if (waterJar.waterLoadTimer > (waterJar.waterLoadTime - waterJar.unitTime)) {  // modify this line
          break;
        }

        if (!waterJar.waterPumpOn) break;

        waterJar.waterLoadTimer += waterJar.unitTime;
        vTaskDelay(waterJar.unitTime);
      }

      while (!waterJar.waterPumpOn) {
        if (waterJar.waterLoadTimer < waterJar.unitTime) {
          break;
        }

        if (waterJar.waterPumpOn) break;

        waterJar.waterLoadTimer -= waterJar.unitTime;
        vTaskDelay(waterJar.unitTime);
      }

      vTaskDelay(200);
    }
  }


  static void tFeedWater(void* taskParams) {

    WaterJarController& waterJar = WaterJarController::getInstance();

    //이미 물주고있다면 무시
    if (waterJar.waterPumpOn) vTaskDelete(NULL);

    int operationTime = (*(int*)taskParams);

    LOGF("물주기 %d 초 동안 작동\n", operationTime);

    waterJar.on();                                                                    //물펌프 켜기
    while (waterJar.waterLoadTimer < (waterJar.waterLoadTime - waterJar.unitTime)) {  //물이 출수구까지 차오를때까지 대기
      if (!waterJar.readWaterLevel()) break;
      vTaskDelay(waterJar.unitTime);
    }


    int timer = 0;
    while (waterJar.waterPumpOn) {
      //지정시간이 되었는지 확인
      if (timer >= operationTime) break;
      if (!waterJar.readWaterLevel()) break;

      timer += 250;
      vTaskDelay(250);
    }

    waterJar.off();
    vTaskDelete(NULL);
    delete (int*)taskParams;
  }

  void on() {
    // if (waterPumpOn) return;

    DB_Manager& dbManager = DB_Manager::getInstance();

    digitalWrite(PIN_WATERJAR_RELAY_SIGNAL, HIGH);
    waterPumpOn = true;
    dbManager.execute("update plant_manage set w_on=1");
    LOGF("물펌프 켜짐, 현재 로드 양 : %d/%d\n", waterLoadTimer, waterLoadTime);
  }

  void off() {
    // if (!waterPumpOn) return;

    DB_Manager& dbManager = DB_Manager::getInstance();

    digitalWrite(PIN_WATERJAR_RELAY_SIGNAL, LOW);
    waterPumpOn = false;
    dbManager.execute("update plant_manage set w_on=0");
    LOGF("물펌프 꺼짐, 현재 로드 양 : %d/%d\n", waterLoadTimer, waterLoadTime);
  }

  int readWaterLevel() {
    return !digitalRead(PIN_WATERJAR_READ_WATERLEVEL);
  }

  void onWaterJarEmpty() {
    //물통이 비었을 때 처리될 코드 작성
    LOGLN("물통에 물 부족함");
  }

  void setWaterLoadTime(int time) {
    waterLoadTime = time;
    LOGF("WaterLoadTime %d 로 설정됨!", waterLoadTime);
  }

  bool isRunning() {
    return waterPumpOn;
  }
};

WaterJarController* WaterJarController::instance = nullptr;  // 정적 변수 초기화



//-------------------------------------------------------------
#endif  //__WATER_JAR_CONTROLLER_H__