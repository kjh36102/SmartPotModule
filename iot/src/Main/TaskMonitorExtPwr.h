// #include "esp32-hal-gpio.h"
#ifndef __TASK_MONITOR_EXT_PWR_H__
#define __TASK_MONITOR_EXT_PWR_H__

//-------------------------------------------------------------
#include <Arduino.h>

#include <queue>
#include <functional>

#define PIN_EXTPWR_ON_R 14
#define PIN_SHUTDOWN_O 12

// 함수를 저장할 큐
std::queue<std::function<void()>> shutdownFuncQueue;

//-------------------------------------------------------------
#define LOGKEY "TaskMonitorExtPwr.h"
#include "Logger.h"
//-------------------------------------------------------------

void appendShutdownProcess(std::function<void()> func) {
  // 큐에 함수를 추가
  shutdownFuncQueue.push(func);
  LOG(F("종료큐에 함수 저장됨. 현재 개수: "));
  LOGLN(shutdownFuncQueue.size());
}

void initTaskMonitorExtPwr() {
  pinMode(PIN_EXTPWR_ON_R, INPUT);
  pinMode(PIN_SHUTDOWN_O, OUTPUT);

  digitalWrite(PIN_SHUTDOWN_O, LOW);
}

void tMonitorExtPwr(void* taskParams) {
  initTaskMonitorExtPwr();
  for (;;) {

    if (!digitalRead(PIN_EXTPWR_ON_R)) {
      LOG(F("외부전원 차단 감지됨! 실행할 함수 수: "));
      LOGLN(shutdownFuncQueue.size());
      while (!shutdownFuncQueue.empty()) {
        // 큐의 앞에 있는 함수를 호출하고 제거
        shutdownFuncQueue.front()();
        shutdownFuncQueue.pop();
      }
      digitalWrite(PIN_SHUTDOWN_O, HIGH);
    }

    vTaskDelay(2500);
  }
}

//-------------------------------------------------------------
#endif  //__TASK_MONITOR_EXT_PWR_H__