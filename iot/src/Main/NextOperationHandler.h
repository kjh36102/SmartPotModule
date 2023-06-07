#ifndef __NEXT_OPERATION_HANDLER_H__
#define __NEXT_OPERATION_HANDLER_H__

//-------------------------------------------------------------
#include <Arduino.h>

#include "MultitaskRTOS.h"
#include "DB_Manager.h"
#include "TimeUpdater.h"
#include "LightStandController.h"
#include <map>

//-------------------------------------------------------------
#define LOGKEY "NextOperationHandler.h"
#include "Logger.h"
//-------------------------------------------------------------

class NextOperData {
public:
  byte id;
  int ud;
  char st[6];
  int val;
  char no[20];
};

class NextOperationHandler {
private:
  static NextOperationHandler* instance;

  TimeUpdater* timeUpdater;
  DB_Manager* dbManager;
  LightStandController* lightStand;

  int maxRecordCount = 6;
  static bool runningFlag;


  // ...
  NextOperationHandler() {
    timeUpdater = &TimeUpdater::getInstance();
    dbManager = &DB_Manager::getInstance();
    lightStand = &LightStandController::getInstance();

    // createAndRunTask(NextOperationHandler::tCheckManageLightTimestamp, "tCheckManageLightTimestamp", 4000);
  }

public:

  static void startTasks() {
    if (!runningFlag) {
      runningFlag = true;
      createAndRunTask(NextOperationHandler::tCheckManageLightTimestamp, "tCheckManageLightTimestamp", 6000);
    }
  }

  static void stopTasks() {
    runningFlag = false;
  }

  static NextOperationHandler& getInstance() {
    if (instance == nullptr) {
      instance = new NextOperationHandler();
    }

    return *instance;
  }

  int getMaxRecordCount() {
    return maxRecordCount;
  }


  std::vector<NextOperData> getDataFromDB(const char* tableName, const char* valueColumnName) {
    std::vector<NextOperData> data;

    std::string sqlCommand = "SELECT * FROM " + std::string(tableName);
    if (dbManager->execute(sqlCommand.c_str()) == SQLITE_OK) {
      char* result = dbManager->getResult();

      // Parse the result JSON
      StaticJsonDocument<DB_RESULT_BUFFER_SIZE> doc;  // Change the size to fit your JSON
      DeserializationError error = deserializeJson(doc, result);

      if (error) {
        if(result == NULL)
          LOGLN("레코드 없음");
        else
          LOGLN("Failed to parse JSON");
      } else {
        JsonArray array = doc.as<JsonArray>();
        for (JsonObject obj : array) {
          NextOperData nod;
          nod.id = obj["id"].as<byte>();
          nod.ud = obj["ud"].as<int>();
          strncpy(nod.st, obj["st"].as<const char*>(), sizeof(nod.st) - 1);
          nod.st[sizeof(nod.st) - 1] = '\0';  // Ensure null termination
          nod.val = obj[valueColumnName].as<int>();
          strncpy(nod.no, obj["no"].as<const char*>(), sizeof(nod.no) - 1);
          nod.no[sizeof(nod.no) - 1] = '\0';  // Ensure null termination
          data.push_back(nod);
        }
      }
    } else {
      LOGLN("SQL execution failed");
    }

    return data;
  }

  static void tCheckManageLightTimestamp(void* taskParams) {
    LOGLN("Starting tCheckManageLightTimestamp...");
    NextOperationHandler& handler = NextOperationHandler::getInstance();

    // Calculate the time until the next minute
    int secondsToNextMinute = 60 - handler.timeUpdater->getCurrentSeconds();
    vTaskDelay(secondsToNextMinute * 1000 / portTICK_PERIOD_MS);

    // Get the current tick count
    TickType_t xLastWakeTime = xTaskGetTickCount();

    // Define the delay period in ticks (1000 ticks per second, so 60000 for one minute)
    const TickType_t xDelay = 60000 / portTICK_PERIOD_MS;

    while (NextOperationHandler::runningFlag) {

      // Get data from the database
      std::vector<NextOperData> data = handler.getDataFromDB("manage_light", "ls");

      if (!data.empty()) {
        // Get the current timestamp
        String currentTimestamp = handler.timeUpdater->getCurrentTimeNoSecond();

        LOGF("Running tCheckManageLightTimestamp... CurrentTime: %s\n", currentTimestamp.c_str());

        // Go through each record and check if the timestamp matches the current time
        for (NextOperData& record : data) {
          if (strcmp(record.no, currentTimestamp.c_str()) == 0) {
            // If the timestamp matches, control the light based on the value
            LOGF("\ttCheckManageLightTimestamp founds same time: %s\n", record.no);
            if (record.val == 1) {
              handler.lightStand->on();
            } else {
              handler.lightStand->off();
            }

            //디버깅용, 원래 0 못들어옴
            if (record.ud == 0) record.ud = 1;

            //ud, st, 그리고 현재시간을 이용해서 no 업데이트
            String no = handler.timeUpdater->getNextOperationTime((int)record.ud, String(record.st), false);

            char sqlBuffer[100];

            sprintf(sqlBuffer, "update manage_light set no='%s' where id=%d", no.c_str(), record.id);

            handler.dbManager->execute(sqlBuffer);
            LOGLN("nextHandler Light no updated");
          }
        }
      }

      // Delay until the next period
      vTaskDelayUntil(&xLastWakeTime, xDelay);
    }

    vTaskDelete(NULL);
  }


  // ...
};

// Initialization
NextOperationHandler* NextOperationHandler::instance = nullptr;
bool NextOperationHandler::runningFlag = false;

//-------------------------------------------------------------
#endif  //__NEXT_OPERATION_HANDLER_H__
