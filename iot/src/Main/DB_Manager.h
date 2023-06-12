#ifndef __DB_MANAGER_H__
#define __DB_MANAGER_H__

//-------------------------------------------------------------
#include <Arduino.h>

#include <stdio.h>
#include <stdlib.h>
#include <sqlite3.h>
#include <SPI.h>
#include <FS.h>
#include "SD.h"
#include "ArduinoJson.h"

//-------------------------------------------------------------
#define LOGKEY "DB_Manager.h"
#include "Logger.h"
//-------------------------------------------------------------

#define DB_RESULT_BUFFER_SIZE 1024

class DB_Manager {
private:
  sqlite3* DB;
  char* DBErrMsg = 0;
  char global_result[DB_RESULT_BUFFER_SIZE];  // 전역 결과 문자열
  bool stateOpenDB = false;
  bool sdInitialized = false;  // SD 카드 초기화 상태

  static DB_Manager* instance;  // 싱글톤 인스턴스

  static int callbackOnExecuteDB(void* instance, int argc, char** argv, char** azColName) {
    DB_Manager* self = reinterpret_cast<DB_Manager*>(instance);

    size_t remainingBufferSize = DB_RESULT_BUFFER_SIZE;

    // 만약 처음으로 결과를 추가한다면 global_result를 초기화하고, "["로 시작
    if (self->global_result[0] == '\0') {
      strncat(self->global_result, "[\n", remainingBufferSize);
      remainingBufferSize -= strlen("[\n") + 1;  // considering null termination
    } else {
      // 이전 행이 있으면, 콤마를 추가하고 줄을 바꿈
      strncat(self->global_result, ",\n", remainingBufferSize);
      remainingBufferSize -= strlen(",\n") + 1;  // considering null termination
    }

    // 각 행의 결과를 JSON 형식으로 추가
    strncat(self->global_result, "  {", remainingBufferSize);
    remainingBufferSize -= strlen("  {") + 1;  // considering null termination

    for (int i = 0; i < argc; i++) {
      const char* colName = azColName[i];
      const char* data = argv[i] ? argv[i] : "NULL";

      char buf[512];  // This size should be sufficient for each column's data
      snprintf(buf, sizeof(buf), "\"%s\": \"%s\"", colName, data);
      strncat(self->global_result, buf, remainingBufferSize);
      remainingBufferSize -= strlen(buf) + 1;  // considering null termination

      // 마지막이 아닌 경우 콤마를 추가
      if (i < argc - 1) {
        strncat(self->global_result, ", ", remainingBufferSize);
        remainingBufferSize -= strlen(", ") + 1;  // considering null termination
      }
    }

    // 각 행의 JSON 객체 닫기
    strncat(self->global_result, "}", remainingBufferSize);
    remainingBufferSize -= strlen("}") + 1;  // considering null termination

    return 0;
  }

  int executeSqlHelper(const char* sql) {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 SQL문 실행 불가능");
      return SQLITE_ERROR;
    }

    // global_result 변수의 첫 문자를 null로 초기화
    global_result[0] = '\0';

    LOGF("SQL 실행: %s\n", sql);

    unsigned long start = millis();

    int rc = sqlite3_exec(DB, sql, callbackOnExecuteDB, this, &DBErrMsg);

    if (rc != SQLITE_OK) {
      if (rc == SQLITE_IOERR) {
        // 디스크 I/O 오류 발생 시 DB를 닫고 다시 열기
        close();
        open("data");

        // 다시 실행
        rc = sqlite3_exec(DB, sql, callbackOnExecuteDB, this, &DBErrMsg);
      }

      if (rc != SQLITE_OK) {
        LOGF("SQL 오류: %s\n", DBErrMsg);
      } else {
        LOGLN("SQL 실행됨");
      }
    }

    sqlite3_free(DBErrMsg);

    Serial.print(F("\t소요 시간(ms):"));
    Serial.println(millis() - start);
    return rc;
  }


  // 생성자는 private로 설정
  DB_Manager() {
  }

public:

  // 다른 복사 생성자와 할당 연산자를 삭제
  DB_Manager(const DB_Manager&) = delete;
  DB_Manager& operator=(const DB_Manager&) = delete;

  // getInstance 메서드를 통해 싱글톤 인스턴스를 가져오거나 생성
  static DB_Manager& getInstance() {
    if (instance == nullptr) {
      instance = new DB_Manager();
    }
    return *instance;
  }

  bool open(String dbName) {
    if (!sdInitialized) {
      if (!SD.begin()) {
        LOGLN("SD 카드 초기화 실패");
      } else {
        LOGLN("SD 카드 초기화 성공");
        sdInitialized = true;
      }
    }


    if (stateOpenDB) {
      LOGLN("DB열기 실패: DB가 이미 열려있음");
    } else {

      int rc = sqlite3_open(("/sd/" + dbName + ".db").c_str(), &DB);

      if (rc) {
        LOG("DB열기 실패: ");
        LOGLN(sqlite3_errmsg(DB));
      } else {
        stateOpenDB = true;
        LOGLN("DB 열기 성공!");
        return true;
      }
    }

    return false;
  }

  bool close() {
    if (!stateOpenDB) {
      LOGLN("DB닫기 실패: DB가 열려있지 않아 닫을 수 없음");
    } else {
      int rc = sqlite3_close(DB);

      if (rc) {
        LOG("DB닫기 실패: ");
        LOGLN(sqlite3_errmsg(DB));
      } else {
        stateOpenDB = false;
        LOGLN("DB 닫기 성공!");

        if (sdInitialized) {
          SD.end();  //sd카드 연결해제
          sdInitialized = false;
          LOGLN("SD 카드 연결 해제됨");

          return true;
        }
      }
    }

    return false;
  }

  bool isOpened() {
    return stateOpenDB;
  }

  char* getResult() {
    strcat(global_result, "\n]");
    return global_result;
  }

  int execute(const char* sql) {
    return executeSqlHelper(sql);
  }

  int execute(const __FlashStringHelper* sql) {
    const char* sqlPtr = reinterpret_cast<const char*>(sql);
    return executeSqlHelper(sqlPtr);
  }


  int getTableRecordCount(const char* tableName) {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 레코드 개수 조회 불가능");
      return -2;
    }

    char query[100];
    sprintf(query, "SELECT COUNT(*) FROM %s", tableName);

    int rc = execute(query);

    if (rc == SQLITE_OK) {
      char* results = getResult();

      DynamicJsonDocument doc(DB_RESULT_BUFFER_SIZE);
      DeserializationError err = deserializeJson(doc, results);

      // 추가: 오류 체크
      if (err) {
        Serial.print(F("deserializeJson() failed with code "));
        Serial.println(err.c_str());
        doc.clear();  // 실패했더라도 사용한 메모리는 해제
        return -3;
      }

      const char* countStr = doc[0]["COUNT(*)"];  // count(*) 값을 가져옴
      int recordCount = atoi(countStr);           // 문자열을 정수로 변환

      doc.clear();  // 파싱이 끝나면 사용한 메모리를 해제
      return recordCount;
    } else {
      return -1;
    }
  }

  void prepareTable(const char* tableName, const __FlashStringHelper* tableSql, const __FlashStringHelper* dataSql, bool dataEssential = true) {
    int count;
    count = getTableRecordCount(tableName);

    if (count == -2) {
      LOGLN("DB가 열려있지 않아 테이블 초기화 불가능");
      return;
    } else if (count > 0) {
      LOGF("테이블 %s 이미 초기화됨\n", tableName);
      return;
    }

    if (count == -1) {  // 테이블이 없는 경우 테이블 생성 후 데이터생성
      execute(reinterpret_cast<const char*>(tableSql));
      if (dataEssential) execute(reinterpret_cast<const char*>(dataSql));
    } else if (count == 0) {  //테이블이 있는경우는 데이터만 생성
      if (dataEssential) execute(reinterpret_cast<const char*>(dataSql));
    }

    LOGF("테이블 %s 초기화 완료\n", tableName);
  }


  int dropAllTables() {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 테이블 삭제 불가능");
      return -1;
    }

    const char* selectSql = "SELECT name FROM sqlite_master WHERE type='table';";

    int rc = execute(selectSql);

    if (rc != SQLITE_OK) {
      LOGF("테이블 조회 실패\n");
      return rc;
    }

    // select문의 결과 얻기
    char* results = getResult();

    // 결과 파싱
    DynamicJsonDocument doc(DB_RESULT_BUFFER_SIZE);
    DeserializationError err = deserializeJson(doc, results);

    if (err) {
      Serial.print(F("deserializeJson() failed with code "));
      Serial.println(err.c_str());
      return -3;
    }

    // 각 테이블에 대해 DROP TABLE 문 실행
    for (JsonVariant value : doc.as<JsonArray>()) {
      const char* tableName = value["name"];
      char dropSql[100];
      sprintf(dropSql, "DROP TABLE %s;", tableName);

      rc = execute(dropSql);
      if (rc != SQLITE_OK) {
        LOGF("테이블 삭제 실패: %s\n", tableName);
      } else {
        LOGF("테이블 삭제 성공: %s\n", tableName);
      }
    }

    return SQLITE_OK;
  }

  // JsonObject를 반환하는 함수 정의
  JsonObject getRowFromJsonArray(char* jsonArray, int rowIndex) {
    // JsonDocument를 생성합니다 (크기는 충분히 크게 설정합니다).
    StaticJsonDocument<DB_RESULT_BUFFER_SIZE> doc;

    // JsonArray 파싱
    auto error = deserializeJson(doc, jsonArray);

    // 에러 체크
    if (error) {
      Serial.print(F("deserializeJson() failed with code "));
      Serial.println(error.c_str());
      return JsonObject();
    }

    // 파싱한 JsonArray
    JsonArray arr = doc.as<JsonArray>();

    // rowIndex가 JsonArray의 범위 내에 있는지 확인
    if (rowIndex < arr.size()) {
      // JsonObject 반환
      return arr[rowIndex].as<JsonObject>();
    } else {
      // rowIndex가 범위를 벗어났다면 빈 JsonObject 반환
      return JsonObject();
    }
  }

  void initTables() {
    prepareTable("soil_data",
                 F("CREATE TABLE IF NOT EXISTS soil_data (id INTEGER PRIMARY KEY AUTOINCREMENT, tm REAL, hm REAL, n REAL, p REAL, k REAL, ph REAL, ec INTEGER, lt INTEGER, ts TEXT DEFAULT (datetime('now','localtime')))"),
                 F("INSERT INTO soil_data(id,tm,hm,n,p,k,ph,ec,lt) VALUES (0,0,0,0,0,0,0,0,0)"));

    prepareTable("wifi_info",
                 F("CREATE TABLE wifi_info (id INTEGER PRIMARY KEY, ssid_ap TEXT, pw_ap TEXT, ssid_sta TEXT, pw_sta TEXT, phone_ip TEXT)"),
                 F("INSERT INTO wifi_info VALUES(0, 'SmartPotModule', '', '', '', '')"));

    prepareTable("plant_manage",
                 F("CREATE TABLE IF NOT EXISTS plant_manage (id INTEGER PRIMARY KEY, w_auto INTEGER, l_auto INTEGER, w_on INTEGER, l_on INTEGER)"),
                 F("INSERT INTO plant_manage VALUES(0, 0, 0, 0, 0)"));

    prepareTable("manage_auto",
                 F("CREATE TABLE IF NOT EXISTS manage_auto (id INTEGER PRIMARY KEY, hm REAL, th REAL, lt INTEGER, dr INTEGER, ot INTEGER, ld INTEGER, cd INTEGER)"),
                 F("INSERT INTO manage_auto VALUES(0, 0, 0, 0, 0, 0, 0, 0)"));

    prepareTable("manage_water",
                 F("CREATE TABLE IF NOT EXISTS manage_water (id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, wt INTEGER, no TEXT)"),
                 NULL,
                 false);

    prepareTable("manage_light",
                 F("CREATE TABLE IF NOT EXISTS manage_light (id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, ls INTEGER, no TEXT)"),
                 NULL,
                 false);
  }
};

DB_Manager* DB_Manager::instance = nullptr;

//-------------------------------------------------------------
#endif  //__DB_MANAGER_H__