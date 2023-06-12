// #include "WiFiType.h"
#ifndef __ROUTERS_STA_H__
#define __ROUTERS_STA_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "NetworkSetup.h"
#include "ArduinoJson.h"
#include "SoilUpdater.h"
#include "DB_Manager.h"
#include "TimeUpdater.h"
#include "NextOperationHandler.h"
#include "WaterJarController.h"

//-------------------------------------------------------------
#define LOGKEY "RoutersSTA.h"
#include "Logger.h"
//-------------------------------------------------------------


void addToJsonDoc(const String& key, StaticJsonDocument<200>& doc);

void setupSTARouters() {
  serverSTA.on("/hello", HTTP_GET, []() {
    if (WiFi.status() != WL_CONNECTED) {
      serverSTA.send(200, HTTP_MIME, "err|0|Not connected to external network yet.");
      return;
    }

    if (connectPhase < ConnectPhase::COMPLETE) {
      connectPhase = ConnectPhase::COMPLETE;
      smartphoneIP = serverSTA.client().remoteIP();

      serverAP.stop();
      WiFi.softAPdisconnect(true);  //핫스팟끄기

      serverSTA.send(200, HTTP_MIME, "All connected!");


      DB_Manager& dbManager = DB_Manager::getInstance();

      char sqlBuffer[256];
      sprintf(sqlBuffer, "update wifi_info set ssid_sta='%s', pw_sta='%s', phone_ip='%s'", STA_SSID.c_str(), STA_PW.c_str(), smartphoneIP.toString().c_str());
      auto rc = dbManager.execute(sqlBuffer);

      if (rc != SQLITE_OK) {
        serverSTA.send(200, HTTP_MIME, "err[1]Update sql failed.");
        return;
      }

      if (!TimeUpdater::getInstance().updateCurrentTime()) {  //연결되면 현재시간 업데이트
        //실패시
        serverSTA.send(200, HTTP_MIME, "ok|1|Connected but, Internet is not available.");
      } else {
        //성공시
        serverSTA.send(200, HTTP_MIME, "ok|0|All connected!");
      }

      SoilUpdater::getInstance().on();
      NextOperationHandler::startTasks();

      LOGLN(F("성공적으로 앱과 연결됨!"));
      LOGF("\t스마트폰IP: %s\n\t외부네트워크IP: %s\n", smartphoneIP.toString().c_str(), WiFi.localIP().toString().c_str());
    } else {
      serverSTA.send(200, HTTP_MIME, "ok|2|hello!");
    }
  });

  serverSTA.on("/manageAdd", HTTP_GET, []() {
    if (hasValidArg(serverSTA, "table") && hasValidArg(serverSTA, "ud") && hasValidArg(serverSTA, "st") && hasValidArg(serverSTA, "ls", false) && hasValidArg(serverSTA, "wt", false) && hasValidArg(serverSTA, "setNearest")) {
      DB_Manager& dbManager = DB_Manager::getInstance();

      String tableName = serverSTA.arg("table").c_str();

      // 테이블 레코드 개수 확인
      int tableCount = dbManager.getTableRecordCount(tableName.c_str());

      NextOperationHandler& nextHandler = NextOperationHandler::getInstance();

      if (tableCount >= nextHandler.getMaxRecordCount()) {
        serverSTA.send(200, HTTP_MIME, "err|0|Full");
        return;
      }

      int unitDay = serverSTA.arg("ud").toInt();
      String setTime = serverSTA.arg("st");

      int value = 0;
      char valName[3];
      if (tableName.equals("manage_light")) {
        sprintf(valName, "ls");
        value = serverSTA.arg("ls").toInt();
      } else if (tableName.equals("manage_water")) {
        sprintf(valName, "wt");
        value = serverSTA.arg("wt").toInt();
      }

      bool setNearest = (serverSTA.arg("setNearest").equals("true")) ? true : false;

      // 예외 처리: unitDay가 0이하인 경우
      if (unitDay <= 0) {
        serverSTA.send(200, HTTP_MIME, "err|1|Invalid unitDay");
        return;
      }

      TimeUpdater& timeUpdater = TimeUpdater::getInstance();

      // 예외 처리: setTime의 형식이 %H:%M이 아닌 경우
      if (!timeUpdater.isValidSetTimeFormat(setTime)) {
        serverSTA.send(200, HTTP_MIME, "err|2|Invalid setTime format");
        return;
      }

      // next operation 계산
      String nextOperationTime = timeUpdater.getNextOperationTime(unitDay, setTime, setNearest);

      // db에 업데이트
      char sqlBuffer[200];
      sprintf(sqlBuffer, "insert into %s(ud, st, %s, no) values(%d, '%s', %d, '%s')", tableName.c_str(), valName, unitDay, setTime.c_str(), value, nextOperationTime.c_str());

      if (dbManager.execute(sqlBuffer) == SQLITE_OK) {
        serverSTA.send(200, HTTP_MIME, "ok|0|Success");
      } else {
        serverSTA.send(200, HTTP_MIME, "err|3|Failed to execute insert sql");
      }
    } else {
      serverSTA.send(200, HTTP_MIME, "err|4|Error on parsing query args.");
    }
  });

  //센서데이터 바로 측정
  serverSTA.on("/measureNow", HTTP_GET, []() {
    SoilUpdater& soilUpdater = SoilUpdater::getInstance();

    if (soilUpdater.measureNow()) {
      serverSTA.send(200, HTTP_MIME, "ok|0|Success");
    } else {
      serverSTA.send(200, HTTP_MIME, "err|0|Failed");
    }
  });

  //DB 테이블 데이터 가져오기
  serverSTA.on("/getTableData", HTTP_GET, []() {
    if (hasValidArg(serverSTA, "name")) {
      DB_Manager& dbManager = DB_Manager::getInstance();


      String tableName = serverSTA.arg("name");

      // 테이블 레코드 개수 확인
      int tableCount = dbManager.getTableRecordCount(tableName.c_str());

      if (tableCount < 0) {
        serverSTA.send(200, HTTP_MIME, (String("err|0|error while get table count. code is|") + String(tableCount)).c_str());
        return;
      } else if (tableCount == 0) {
        if (!(tableName.equals("manage_light") || tableName.equals("manage_water"))) {
          serverSTA.send(200, HTTP_MIME, "err|1|this table can't be empty");
        } else {
          serverSTA.send(200, HTTP_MIME, "ok|1|no contents");
        }
        return;
      }

      // db에서 json 가져오기
      char sqlBuffer[50];
      sprintf(sqlBuffer, "select * from %s order by id asc", tableName.c_str());

      if (dbManager.execute(sqlBuffer) == SQLITE_OK) {
        serverSTA.send(200, HTTP_MIME, (String("ok|0|") + String(dbManager.getResult())));
      } else {
        serverSTA.send(200, HTTP_MIME, "err|2|failed to execute select sql");
      }
    }
  });

  serverSTA.on("/manageDelete", HTTP_GET, []() {
    if (hasValidArg(serverSTA, "table") && hasValidArg(serverSTA, "id")) {
      String tableName = serverSTA.arg("table");

      DB_Manager& dbManager = DB_Manager::getInstance();
      char sqlBuffer[50];

      //table에서 id값 오름차순으로 가져오기
      sprintf(sqlBuffer, "select id from %s order by id asc", tableName.c_str());

      dbManager.execute(sqlBuffer);

      char* result = dbManager.getResult();

      DynamicJsonDocument doc(DB_RESULT_BUFFER_SIZE);  // 적절한 크기로 설정
      DeserializationError error = deserializeJson(doc, result);

      // 파싱 결과 확인
      if (error) {
        LOGF("Failed to parse JSON on /manageDelete %s for records: %s", tableName.c_str(), error.c_str());
        serverSTA.send(200, HTTP_MIME, "err|0|Failed to parse sql args. May be there is no records.");
        doc.clear();  //dynamic 초기화
        return;
      }

      // JsonArray에서 각 요소를 정수로 추출하여 배열로 저장
      JsonArray recordJsonArray = doc.as<JsonArray>();
      int recordArraySize = recordJsonArray.size();
      int recordArray[recordArraySize];

      LOGLN("recordArray");
      for (int i = 0; i < recordArraySize; i++) {
        recordArray[i] = recordJsonArray[i]["id"].as<int>();
        LOGLN(recordArray[i]);
      }

      doc.clear();  //dynamic 초기화

      String ids = serverSTA.arg("id");

      // DynamicJsonDocument doc(50);  // 적절한 크기로 설정
      error = deserializeJson(doc, ids);

      // 파싱 결과 확인
      if (error) {
        LOGF("Failed to parse JSON on /manageDelete %s for query: %s", tableName.c_str(), error.c_str());
        serverSTA.send(200, HTTP_MIME, "err|1|Failed to parse ID args. Check the format.");
        doc.clear();  //dynamic 초기화
        return;
      }

      // JsonArray에서 각 요소를 정수로 추출하여 배열로 저장
      JsonArray idJsonArray = doc.as<JsonArray>();
      int idArraySize = idJsonArray.size();

      int maxRecordCount = NextOperationHandler::getInstance().getMaxRecordCount();

      if (idArraySize > maxRecordCount) {
        serverSTA.send(200, HTTP_MIME, "err|2|id list len of query is over the max size");
        doc.clear();  //dynamic 초기화
        return;
      }

      int idArray[idArraySize];
      LOGLN("idArray");
      for (int i = 0; i < idArraySize; i++) {
        idArray[i] = idJsonArray[i].as<int>();
        LOGLN(idArray[i]);
      }

      // 역순으로 제거
      for (int i = idArraySize - 1; i >= 0; i--) {
        if (idArray[i] < 0 /*|| idArray[i] >= maxRecordCount*/) {
          LOGLN("id list from query range invalid");
          serverSTA.send(200, HTTP_MIME, "err|3|ID list from query range invalid");
          doc.clear();
          return;
        }
        sprintf(sqlBuffer, "delete from %s where id=%d", tableName.c_str(), recordArray[idArray[i]]);

        if (dbManager.execute(sqlBuffer) != SQLITE_OK) {
          LOGLN("delete query failed.");
          serverSTA.send(200, HTTP_MIME, "err|4|delete query failed.");
          doc.clear();
          return;
        }
      }

      doc.clear();
      serverSTA.send(200, HTTP_MIME, "ok|0|Removed");
    }
  });

  serverSTA.on("/controlLight", HTTP_GET, []() {
    if (hasValidArg(serverSTA, "state")) {
      String stateArg = serverSTA.arg("state");

      LightStandController& lightStand = LightStandController::getInstance();

      if (stateArg.equals("true")) {
        lightStand.on();
      } else {
        lightStand.off();
      }

      serverSTA.send(200, HTTP_MIME, "ok|0|Success");
    } else {
      serverSTA.send(200, HTTP_MIME, "err|0|Not enough query args.");
    }
  });

  serverSTA.on("/water", HTTP_GET, []() {
    WaterJarController& waterJar = WaterJarController::getInstance();

    //db에서 ot값 가져오기

    DB_Manager& dbManager = DB_Manager::getInstance();
    dbManager.execute("select ot from manage_auto");
    JsonObject row = dbManager.getRowFromJsonArray(dbManager.getResult(), 0);

    int ot = row["ot"];

    if (ot == 0) {
      serverSTA.send(200, HTTP_MIME, "err|1|ot is 0");
      return;
    }

    if (waterJar.feed(ot * 1000)) {
      serverSTA.send(200, HTTP_MIME, "ok|0|Success");
    } else {
      serverSTA.send(200, HTTP_MIME, "err|0|Failed to run watering..");
    }
  });

  serverSTA.on("/setWaterLoad", HTTP_GET, []() {
    if (hasValidArg(serverSTA, "val")) {
      int val = serverSTA.arg("val").toInt();

      WaterJarController& waterJar = WaterJarController::getInstance();

      waterJar.setWaterLoadTime(val);
      serverSTA.send(200, HTTP_MIME, "ok|0|Success");
    } else {
      serverSTA.send(200, HTTP_MIME, "err|0|Not enough query args.");
    }
  });

  //manageAuto 값 변경
  serverSTA.on("/manageAutoSet", HTTP_GET, []() {
    DB_Manager& dbManager = DB_Manager::getInstance();

    String sqlQuery = "UPDATE manage_auto SET ";

    bool isFirst = true;
    if (hasValidArg(serverSTA, "hm")) {
      if (!isFirst) {
        sqlQuery += ", ";
      }
      isFirst = false;
      sqlQuery += "hm = " + serverSTA.arg("hm");
    }
    if (hasValidArg(serverSTA, "th")) {
      if (!isFirst) {
        sqlQuery += ", ";
      }
      isFirst = false;
      sqlQuery += "th = " + serverSTA.arg("th");
    }
    if (hasValidArg(serverSTA, "lt")) {
      if (!isFirst) {
        sqlQuery += ", ";
      }
      isFirst = false;
      sqlQuery += "lt = " + serverSTA.arg("lt");
    }
    if (hasValidArg(serverSTA, "dr")) {
      if (!isFirst) {
        sqlQuery += ", ";
      }
      isFirst = false;
      sqlQuery += "dr = " + serverSTA.arg("dr");
    }
    if (hasValidArg(serverSTA, "ot")) {
      if (!isFirst) {
        sqlQuery += ", ";
      }
      isFirst = false;
      sqlQuery += "ot = " + serverSTA.arg("ot");
    }
    if (hasValidArg(serverSTA, "ld")) {
      if (!isFirst) {
        sqlQuery += ", ";
      }
      isFirst = false;
      sqlQuery += "ld = " + serverSTA.arg("ld");
    }
    if (hasValidArg(serverSTA, "cd")) {
      if (!isFirst) {
        sqlQuery += ", ";
      }
      isFirst = false;
      sqlQuery += "cd = " + serverSTA.arg("cd");
    }

    if (!isFirst) {
      if (dbManager.execute(sqlQuery.c_str()) != SQLITE_OK) {
        serverSTA.send(200, HTTP_MIME, "err|0|Update sql failed.");
        return;
      }
    }

    serverSTA.send(200, HTTP_MIME, "ok|0|Success");
  });
}


//-------------------------------------------------------------
#endif  //__ROUTERS_STA_H__