#ifndef __POWER_MONITOR_H__
#define __POWER_MONITOR_H__

#include <Arduino.h>
#include <queue>
#include <functional>

#define LOGKEY "PowerMonitor.h"
#include "Logger.h"

#define PIN_EXTPWR_ON_R 14
#define PIN_SHUTDOWN_O 12

class PowerMonitor {
public:
  static PowerMonitor& getInstance() {
    static PowerMonitor instance;
    return instance;
  }

  void appendShutdownProcess(std::function<void()> func) {
    shutdownFuncQueue.push(func);
    LOGF("종료큐에 함수 저장됨. 현재 개수: %d\n", shutdownFuncQueue.size());
  }

  void init() {
    pinMode(PIN_EXTPWR_ON_R, INPUT);
    pinMode(PIN_SHUTDOWN_O, OUTPUT);
    digitalWrite(PIN_SHUTDOWN_O, LOW);
  }

  static void taskFunction(void* pvParameters) {
    PowerMonitor::getInstance().run();
  }

  void run() {
    init();
    for (;;) {
      if (!digitalRead(PIN_EXTPWR_ON_R)) {
        LOGF("외부전원 차단 감지됨! 실행할 함수 수: %d\n", shutdownFuncQueue.size());
        while (!shutdownFuncQueue.empty()) {
          shutdownFuncQueue.front()();
          shutdownFuncQueue.pop();
        }
        digitalWrite(PIN_SHUTDOWN_O, HIGH);
      }
      vTaskDelay(2500);
    }
  }

private:
  PowerMonitor() {}
  std::queue<std::function<void()>> shutdownFuncQueue;
};

#endif  //__POWER_MONITOR_H__
