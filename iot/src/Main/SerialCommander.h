#ifndef SERIAL_COMMANDER_H
#define SERIAL_COMMANDER_H

//-------------------------------------------------------------

#include <Arduino.h>
#include <map>
#include <functional>

//-------------------------------------------------------------
#define LOGKEY "SerialCommander.h"
#include "Logger.h"
//-------------------------------------------------------------

class SerialCommander {
private:
  std::map<String, std::function<void(String)>> callbacks;

  static SerialCommander* instance;

  // Private constructor for singleton
  SerialCommander() {
  }

public:
  // Delete copy constructor and assignment operator
  SerialCommander(const SerialCommander&) = delete;
  SerialCommander& operator=(const SerialCommander&) = delete;

  // Public method to access the singleton instance
  static SerialCommander& getInstance() {
    if (instance == nullptr) {
      instance = new SerialCommander();

      instance->registerCommands();

      createAndRunTask(SerialCommander::taskFunction, "SerialCommander", 20000);
    }
    return *instance;
  }

  void registerCallback(String command, std::function<void(String)> func) {
    if (callbacks.find(command) == callbacks.end()) {
      callbacks[command] = std::move(func);
    } else {
      LOGLN("Callback already exists for this command!");
    }
  }

  static void taskFunction(void* taskParam) {
    SerialCommander::getInstance().run();
  }

  void run() {
    for (;;) {
      if (Serial.available()) {
        String input = Serial.readStringUntil('\n');
        int dividerIndex = input.indexOf("://");

        if (dividerIndex == -1) continue;  // Invalid command

        String command = input.substring(0, dividerIndex);
        String argument = input.substring(dividerIndex + 3);  // get argument after '://'

        auto it = callbacks.find(command);
        if (it != callbacks.end()) {  // command found
          it->second(argument);       // Execute the callback with the argument
        } else {
          LOGLN("Command not found!");
        }
      }

      vTaskDelay(250);
    }
  }

  void registerCommands() {

    SerialCommander& commander = SerialCommander::getInstance();

    commander.registerCallback("time", [](String line) {
      TimeUpdater& timeUpdater = TimeUpdater::getInstance();

      if (line.equals("update")) {
        // Time update
        timeUpdater.updateCurrentTime();
        LOGLN(timeUpdater.getCurrentTime());
      } else if (line.equals("print")) {
        LOGLN(timeUpdater.getCurrentTime());
      } else {
        // line이 nextop ud=1&st=22:00&ls=1&setNearest=false 일 때
        if (line.startsWith("nextop")) {
          // line에서 쿼리스트링 파싱
          int unitDay = 0;
          String setTime;
          int lightState = 0;
          bool setNearest = false;

          // line에서 쿼리스트링 추출
          line = line.substring(line.indexOf('?') + 1);

          // '&'를 기준으로 쿼리스트링을 분리하여 처리
          while (line.length() > 0) {
            int ampersandIndex = line.indexOf('&');
            String param;
            if (ampersandIndex != -1) {
              param = line.substring(0, ampersandIndex);
              line = line.substring(ampersandIndex + 1);
            } else {
              param = line;
              line = "";
            }

            // '='를 기준으로 파라미터 이름과 값을 분리하여 처리
            int equalsIndex = param.indexOf('=');
            if (equalsIndex != -1) {
              String paramName = param.substring(0, equalsIndex);
              String paramValue = param.substring(equalsIndex + 1);

              if (paramName.equals("ud")) {
                unitDay = paramValue.toInt();
              } else if (paramName.equals("st")) {
                setTime = paramValue;
              } else if (paramName.equals("ls")) {
                lightState = paramValue.toInt();
              } else if (paramName.equals("setNearest")) {
                setNearest = paramValue.equals("true");
              }
            }
          }

          // next operation 계산
          String nextOperationTime = timeUpdater.getNextOperationTime(unitDay, setTime, setNearest);

          LOGF("NextOperationTime: %s\n", nextOperationTime.c_str());
        }
      }
    });

    commander.registerCallback("lightstand", [](String line) {
      LightStandController& lightStandCtrl = LightStandController::getInstance();
      if (line.equals("on")) {
        lightStandCtrl.on();
      } else if (line.equals("off")) {
        lightStandCtrl.off();
      } else {
        lightStandCtrl.setUnionDuty(line.toInt());
      }
    });

    commander.registerCallback("db", [](String line) {
      DB_Manager& dbManager = DB_Manager::getInstance();  // DB_Manager 인스턴스 얻기

      if (line.equals("open")) {
        dbManager.open("data");
      } else if (line.equals("close")) {
        dbManager.close();
      }

      else if (line.equals("createtables")) {
        if (!dbManager.isOpened()) {
          LOGLN("DB가 열려있지 않아 createtables명령 수행 불가능");
          return;
        }

        dbManager.initTables();
      } else if (line.equals("droptables")) {
        dbManager.dropAllTables();
      } else if (line.equals("count")) {
        LOG("COUNT COUNT: ");
        LOGLN(dbManager.getTableRecordCount("manage_light"));
      } else {
        if (dbManager.execute(line.c_str()) == SQLITE_OK) {  // SQL 명령 실행
          char* result = dbManager.getResult();              // 결과 받기
          LOGLN(result);                                     // 결과 출력
        } else {
          LOGLN("SQL 수행 실패");
        }
      }
    });

    commander.registerCallback("network", [](String line) {
      if (line.equals("setup")) {
        connectPhase = ConnectPhase::SETUP;
        initNetwork(true);
      }
    });

    commander.registerCallback("system", [](String line) {
      if (line.equals("free")) {
        LOGF("FreeHeapMemory: %u\n", esp_get_free_heap_size());
      } else if (line.equals("reset")) {
        PowerMonitor::getInstance().executeAll();
        esp_restart();
      }
    });

    commander.registerCallback("soilupdater", [](String line) {
      SoilUpdater& soilUpdater = SoilUpdater::getInstance();

      if (line.equals("on")) {
        soilUpdater.on();
        LOGLN("SoilUpdater 켜짐");
      } else if (line.equals("off")) {
        soilUpdater.off();
        LOGLN("SoilUpdater 꺼짐");
      } else if (line.equals("measure")) {
        soilUpdater.measureNow(true);
      }
    });

    commander.registerCallback("waterjar", [](String line) {
      WaterJarController& waterJar = WaterJarController::getInstance();

      if (line.equals("on")) {
        waterJar.on();
      } else if (line.equals("off")) {
        waterJar.off();
      } else if (line.startsWith("loadtime")) {
        int index = line.indexOf(' ');

        if (index != -1) {                                 // Ensure that ' ' is found.
          String loadTimeStr = line.substring(index + 1);  // Extracts the part after ' '.
          char* pEnd;
          long loadTime = strtol(loadTimeStr.c_str(), &pEnd, 10);

          if (*pEnd) {
            LOGLN("Invalid number format!");
          } else {
            waterJar.setWaterLoadTime(loadTime);
          }
        } else {
          LOGLN("Missing load time value!");
        }
      } else if (line.equals("level")) {
        LOGF("WaterJar Level: %d\n", waterJar.readWaterLevel());
      } else {
        waterJar.feed(line.toInt());
      }
    });
  }
};

SerialCommander* SerialCommander::instance = nullptr;

#endif  //SERIAL_COMMANDER_H
