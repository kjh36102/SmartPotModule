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
private:

  static PowerMonitor* instance;

  std::queue<std::function<void()>> shutdownFuncQueue;

  PowerMonitor() {
    pinMode(PIN_EXTPWR_ON_R, INPUT);
    pinMode(PIN_SHUTDOWN_O, OUTPUT);
    digitalWrite(PIN_SHUTDOWN_O, LOW);
  }

public:

  static PowerMonitor& getInstance() {
    if (instance == nullptr) {
      instance = new PowerMonitor();
      instance->appendAll();

      createAndRunTask(taskFunction, "PowerMonitor", 10000);
    }

    return *instance;
  }

  void appendShutdownProcess(std::function<void()> func) {
    shutdownFuncQueue.push(func);
    LOGF("종료큐에 함수 저장됨. 현재 개수: %d\n", shutdownFuncQueue.size());
  }

  void appendAll() {
    appendShutdownProcess([]() {
      DB_Manager& dbManager = DB_Manager::getInstance();
      dbManager.close();
    });
  }

  static void taskFunction(void* pvParameters) {
    PowerMonitor::getInstance().run();
  }

  void executeAll() {
    while (!shutdownFuncQueue.empty()) {
      shutdownFuncQueue.front()();
      shutdownFuncQueue.pop();
    }
  }

  void run() {
    init();
    for (;;) {

      //디버깅용 메모리용량확인
      LOGF("\t\t\t\tFreeHeapMemory: %u\n", esp_get_free_heap_size());

      if (!digitalRead(PIN_EXTPWR_ON_R)) {
        LOGF("외부전원 차단 감지됨! 실행할 함수 수: %d\n", shutdownFuncQueue.size());
        executeAll();
        vTaskDelay(1000);
        digitalWrite(PIN_SHUTDOWN_O, HIGH);
      }
      vTaskDelay(2500);
    }
  }
};

PowerMonitor* PowerMonitor::instance = nullptr;

#endif  //__POWER_MONITOR_H__
