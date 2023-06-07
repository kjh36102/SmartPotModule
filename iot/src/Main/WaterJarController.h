#ifndef __WATER_JAR_CONTROLLER_H__
#define __WATER_JAR_CONTROLLER_H__

//-------------------------------------------------------------
#include <Arduino.h>


//-------------------------------------------------------------
#define LOGKEY "WaterJarController.h"
#include "Logger.h"
//-------------------------------------------------------------



class WaterJarController {
private:
  static WaterJarController* instance;  // 싱글톤 객체의 인스턴스를 저장할 정적 변수
  WaterJarController() {}               // 생성자를 private로 선언하여 외부에서의 객체 생성을 막음

public:
  static WaterJarController* getInstance() {
    if (instance == nullptr) {
      instance = new WaterJarController();  // 인스턴스 생성
    }
    return instance;
  }

  // 다른 멤버 함수들 정의
};

WaterJarController* WaterJarController::instance = nullptr;  // 정적 변수 초기화



//-------------------------------------------------------------
#endif  //__WATER_JAR_CONTROLLER_H__