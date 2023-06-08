#ifndef __WATER_JAR_CONTROLLER_H__
#define __WATER_JAR_CONTROLLER_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "MultitaskRTOS"

//-------------------------------------------------------------
#define LOGKEY "WaterJarController.h"
#include "Logger.h"
//-------------------------------------------------------------

#define PIN_WATERJAR_RELAY_SIGNAL 00
#define PIN_WATERJAR_READ_WATERLEVEL 00

class WaterJarController {
private:
  static WaterJarController* instance;  // 싱글톤 객체의 인스턴스를 저장할 정적 변수
  WaterJarController() {
    pinMode(PIN_WATERJAR_RELAY_SIGNAL, OUTPUT);
    pinMode(PIN_WATERJAR_READ_WATERLEVEL, INPUT);

    off();

  }  // 생성자를 private로 선언하여 외부에서의 객체 생성을 막음

  int maxFeedTime = 30;

public:
  static WaterJarController& getInstance() {
    if (instance == nullptr) {
      instance = new WaterJarController();  // 인스턴스 생성
    }
    return *instance;
  }

  // 다른 멤버 함수들 정의

  void directFeed() {
    //DB의 manage_auto테이블의 ot 칼럼값을 읽어와 int로 변환
    int ot = 0;

    //ot (초) 동안 실행되는 task 실행하기
    createAndRunTask(tFeedWater, "TaskFeedWater", 2000, 1, ot);
  }

  static void tFeedWater(void* taskParams) {
    WaterJarController& waterJar = WaterJarController::getInstance();
    int operationTime = (*(int*)taskParams) * 1000;

    LOGf("물주기 %d 초 동안 작동\n", operationTime);

    if (operationTime < 1 || operationTime > waterJar.maxFeedTIme) {
      LOGLN("TaskFeedWater operationTIme range is invalid!");
    }
    //릴레이에 신호를 보내 연결된 물펌프와 솔레노이드를 operation time 만큼 작동시킨다.


    int timer = 0;
    waterJar.on();

    while (true) {
      //물펌프에 물이 있는지 확인
      if (!waterJar.readWaterLevel()) {
        waterJar.onWaterJarEmpty();
        break;
      }

      //지정시간이 되었는지 확인
      if (timer >= operationTime) break;

      timer += 250;
      vTaskDelay(250);
    }

    waterJar.off();
    vTaskDelete(NULL);
  }

  void on() {
    //릴레이에 신호를 보내 연결된 물펌프와 솔레노이드를 켠다.
    digitalWrite(PIN_WATERJAR_RELAY_SIGNAL, HIGH);
    LOGLN("물펌프 켜짐");
  }

  void off() {
    //물펌프를 끈다
    digitalWrite(PIN_WATERJAR_RELAY_SIGNAL, LOW);
    LOGLN("물펌프 꺼짐");
  }

  int readWaterLevel() {
    return digitalRead(PIN_WATERJAR_READ_WATERLEVEL);
  }

  void onWaterJarEmpty(){
    //물통이 비었을 때 처리될 코드 작성
    LOGLN("물통에 물 부족함");
  }
};

WaterJarController* WaterJarController::instance = nullptr;  // 정적 변수 초기화



//-------------------------------------------------------------
#endif  //__WATER_JAR_CONTROLLER_H__