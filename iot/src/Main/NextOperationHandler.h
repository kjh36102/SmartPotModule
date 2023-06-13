#ifndef __NEXT_OPERATION_HANDLER_H__
#define __NEXT_OPERATION_HANDLER_H__

//-------------------------------------------------------------
#include <Arduino.h>

#include "MultitaskRTOS.h"
#include "DB_Manager.h"
#include "TimeUpdater.h"
#include "LightStandController.h"
#include "WaterJarController.h"
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
  WaterJarController* waterJar;

  int maxRecordCount = 6;
  static bool runningFlag;
  char sqlBuffer[100];

  int lightAutoPassedMin = 0;
  int waterAutoAccel = 0;


  // ...
  NextOperationHandler() {
    timeUpdater = &TimeUpdater::getInstance();
    dbManager = &DB_Manager::getInstance();
    lightStand = &LightStandController::getInstance();
    waterJar = &WaterJarController::getInstance();
  }

public:

  static void startTasks() {
    if (!runningFlag) {
      runningFlag = true;
      createAndRunTask(NextOperationHandler::tHandleProcess, "tHandleProcess", 10000);
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

  static void tHandleProcess(void* taskParams) {
    LOGLN("Starting ManualHandler...");
    NextOperationHandler& handler = NextOperationHandler::getInstance();

    // Calculate the time until the next minute
    int secondsToNextMinute = 60 - handler.timeUpdater->getCurrentSeconds();
    vTaskDelay(secondsToNextMinute * 1000 / portTICK_PERIOD_MS);

    // Get the current tick count
    TickType_t xLastWakeTime = xTaskGetTickCount();

    // Define the delay period in ticks (1000 ticks per second, so 60000 for one minute)
    const TickType_t xDelay = 60000 / portTICK_PERIOD_MS;

    while (NextOperationHandler::runningFlag) {

      handler.dbManager->execute("select pm.l_on, pm.w_on, pm.l_auto, pm.w_auto, ma.lt, ma.dr, ma.hm, ma.th from plant_manage pm, manage_auto ma");
      LOGF("DB RESULT: %s\n", handler.dbManager->getResult());
      // JsonObject row = handler.dbManager->getRowFromJsonArray(handler.dbManager->getResult(), 0);
      std::map<std::string, std::string> row = handler.dbManager->getRowFromMap(handler.dbManager->getResult(), 0);

      Serial.print("JsonObjectSize: ");
      Serial.println(row.size());

      // int l_auto = row["l_auto"].as<int>();
      // int w_auto = row["w_auto"].as<int>();
      // int l_on = row["l_on"].as<int>();
      // int w_on = row["w_on"].as<int>();
      // int lt = row["lt"].as<int>();
      // int dr = row["dr"].as<int>();
      // float hm = row["hm"].as<float>();
      // float th = row["th"].as<float>();

      // int l_auto = row["l_auto"].toInt();
      // int w_auto = row["w_auto"].toInt();
      // int l_on = row["l_on"].toInt();
      // int w_on = row["w_on"].toInt();
      // int lt = row["lt"].toInt();
      // int dr = row["dr"].toInt();
      // float hm = row["hm"].toFloat();
      // float th = row["th"].toFloat();

      int l_auto = atoi(row["l_auto"].c_str());
      int w_auto = atoi(row["w_auto"].c_str());
      int l_on = atoi(row["l_on"].c_str());
      int w_on = atoi(row["w_on"].c_str());
      int lt = atoi(row["lt"].c_str());
      int dr = atoi(row["dr"].c_str());
      float hm = atof(row["hm"].c_str());
      float th = atof(row["th"].c_str());



      LOGF("%d, %d, %d, %d, %d, %d, %.2f, %.2f\n", l_auto, w_auto, l_on, w_on, lt, dr, hm, th);

      if (l_auto == 0) {
        handler.handleLightManual();
      } else {
        handler.handleLightAuto(l_on, lt, dr);
      }

      if (w_auto == 0) {
        // handler.handleWaterManual();
      } else {
        // handler.handleWaterAuto(w_on, hm, th);
      }

      // Delay until the next period
      vTaskDelayUntil(&xLastWakeTime, xDelay);
    }

    vTaskDelete(NULL);
  }

  void handleLightAuto(int l_on, int lt, int dr) {
    LOGF("HANDLE LIGHT AUTO l_on:%d, lt:%d, dr:%d\n", l_on, lt, dr);

    //조명등에서 현재 조도값 불러오기
    LightStandController& lightStand = LightStandController::getInstance();
    float currentLux = lightStand.readLightLevel();
    LOGF("Readed currentLux: %.1f\n", currentLux);


    if (currentLux <= lt) {
      LOGLN("pin1");
      lightAutoPassedMin = min(++lightAutoPassedMin, dr);
    } else if (currentLux > lt) {
      LOGLN("pin2");
      lightAutoPassedMin = 0;
    }

    LOGF("lightAutoPassedMin: %d\n", lightAutoPassedMin);
    if (lightAutoPassedMin >= dr) {
      LOGLN("pin3");
      lightStand.on();
    } else {
      LOGLN("pin4");
      lightStand.off();
    }
  }

  void handleLightManual() {
    // Get data from the database
    std::vector<NextOperData> data = getDataFromDB("manage_light", "ls");

    if (!data.empty()) {
      // Get the current timestamp
      String currentTimestamp = timeUpdater->getCurrentTimeNoSecond();

      LOGF("Handling Light Manual... CurrentTime: %s\n", currentTimestamp.c_str());

      // Go through each record and check if the timestamp matches the current time
      for (NextOperData& record : data) {
        if (strcmp(record.no, currentTimestamp.c_str()) == 0) {
          // If the timestamp matches, control the light based on the value
          LOGF("\tHandling Light Manual founds same time: %s\n", record.no);
          if (record.val == 1) {
            lightStand->on();
          } else {
            lightStand->off();
          }

          //디버깅용, 원래 0 못들어옴
          if (record.ud == 0) record.ud = 1;

          //ud, st, 그리고 현재시간을 이용해서 no 업데이트
          String no = timeUpdater->getNextOperationTime((int)record.ud, String(record.st), false);

          sprintf(sqlBuffer, "update manage_light set no='%s' where id=%d", no.c_str(), record.id);

          dbManager->execute(sqlBuffer);
          LOGLN("nextHandler Light no updated");
        }
      }
    }
  }

  void handleWaterAuto(int w_on, int hm, int th) {
    LOGLN("HANDLE WATER AUTO");
  }

  void handleWaterManual() {
    // Get data from the database
    std::vector<NextOperData> data = getDataFromDB("manage_water", "wt");

    if (!data.empty()) {
      // Get the current timestamp
      String currentTimestamp = timeUpdater->getCurrentTimeNoSecond();

      LOGF("Handling Water Manual... CurrentTime: %s\n", currentTimestamp.c_str());

      // Go through each record and check if the timestamp matches the current time
      for (NextOperData& record : data) {
        if (strcmp(record.no, currentTimestamp.c_str()) == 0) {
          // If the timestamp matches, control the light based on the value
          LOGF("\tHandling Water Manual founds same time: %s\n", record.no);

          waterJar->feed(record.val * 1000);

          //디버깅용, 원래 0 못들어옴
          if (record.ud == 0) record.ud = 1;

          //ud, st, 그리고 현재시간을 이용해서 no 업데이트
          String no = timeUpdater->getNextOperationTime((int)record.ud, String(record.st), false);

          sprintf(sqlBuffer, "update manage_water set no='%s' where id=%d", no.c_str(), record.id);

          dbManager->execute(sqlBuffer);
          LOGLN("nextHandler Water no updated");
        }
      }
    }
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
        if (result == NULL)
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
};

// Initialization
NextOperationHandler* NextOperationHandler::instance = nullptr;
bool NextOperationHandler::runningFlag = false;

//-------------------------------------------------------------
#endif  //__NEXT_OPERATION_HANDLER_H__
