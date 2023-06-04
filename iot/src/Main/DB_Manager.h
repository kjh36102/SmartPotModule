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

    // 만약 처음으로 결과를 추가한다면 global_result를 초기화하고, "["로 시작
    if (self->global_result[0] == '\0') {
      strcat(self->global_result, "[\n");
    } else {
      // 이전 행이 있으면, 콤마를 추가하고 줄을 바꿈
      strcat(self->global_result, ",\n");
    }

    // 각 행의 결과를 JSON 형식으로 추가
    strcat(self->global_result, "  {");

    for (int i = 0; i < argc; i++) {
      strcat(self->global_result, "\"");
      strcat(self->global_result, azColName[i]);
      strcat(self->global_result, "\": ");
      strcat(self->global_result, "\"");
      if (argv[i]) {
        strcat(self->global_result, argv[i]);
      } else {
        strcat(self->global_result, "NULL");
      }
      strcat(self->global_result, "\"");

      // 마지막이 아닌 경우 콤마를 추가
      if (i < argc - 1) {
        strcat(self->global_result, ", ");
      }
    }

    // 각 행의 JSON 객체 닫기
    strcat(self->global_result, "}");

    return 0;
  }



  int executeSqlHelper(const char* sql) {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 SQL문 실행 불가능");
      return SQLITE_ERROR;
    }

    // global_result변수의 첫문자를 null로 초기화
    global_result[0] = '\0';

    LOGF("SQL 실행: %s\n", sql);

    unsigned long start = millis();

    int rc = sqlite3_exec(DB, sql, callbackOnExecuteDB, this, &DBErrMsg);

    if (rc != SQLITE_OK) {
      LOGF("SQL 오류: %s\n", DBErrMsg);
    } else {
      LOGLN("SQL 실행됨");
    }

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

  // int getTableRecordCount(const char* tableName) {
  //   if (!stateOpenDB) {
  //     LOGLN("DB가 열려있지 않아 레코드 개수 조회 불가능");
  //     return -2;
  //   }

  //   char query[100];
  //   sprintf(query, "SELECT COUNT(*) FROM %s", tableName);

  //   int rc = execute(query);

  //   if (rc == SQLITE_OK) {
  //     char* results = getResult();

  //     DynamicJsonDocument doc(DB_RESULT_BUFFER_SIZE);
  //     deserializeJson(doc, results);

  //     LOG("PIN: ");
  //     LOGLN(results);

  //     return doc[0][0];
  //   } else {
  //     return -1;
  //   }
  // }

  int isTableRecordExists(const char* tableName) {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 레코드 개수 조회 불가능");
      return -2;
    }

    char query[100];
    sprintf(query, "SELECT EXISTS(SELECT 1 FROM %s WHERE id = 0) AS record_exists", tableName);

    int rc = execute(query);

    if (rc == SQLITE_OK) {
      char* results = getResult();

      DynamicJsonDocument doc(DB_RESULT_BUFFER_SIZE);
      deserializeJson(doc, results);

      int recordExists = doc[0]["record_exists"];
      return recordExists;
    } else {
      return -1;
    }
  }



  void prepareTable(const char* tableName, const __FlashStringHelper* tableSql, const __FlashStringHelper* dataSql, bool dataEssential = true) {
    int count;
    count = isTableRecordExists(tableName);

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


  void prepareTable(const char* tableName, const char* tableSql, const char* dataSql, bool dataEssential = true) {
    int count;
    count = isTableRecordExists(tableName);

    if (count == -2) {
      LOGLN("DB가 열려있지 않아 테이블 초기화 불가능");
      return;
    } else if (count > 0) {
      LOGF("테이블 %s 이미 초기화됨\n", tableName);
      return;
    }

    if (count == -1) {  // 테이블이 없는 경우 테이블 생성 후 데이터생성
      execute(tableSql);
      if (dataEssential) execute(dataSql);
    } else if (count == 0) {  //테이블이 있는경우는 데이터만 생성
      if (dataEssential) execute(dataSql);
    }

    LOGF("테이블 %s 초기화 완료\n", tableName);
  }

  int dropAllTables() {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 테이블 삭제 불가능");
      return -1;
    }

    const char* sql = "SELECT 'DROP TABLE ' || name || ';' FROM sqlite_master WHERE type='table';";

    char** dropStatements;
    int rowCount, colCount;
    char* errMsg = nullptr;

    int rc = sqlite3_get_table(DB, sql, &dropStatements, &rowCount, &colCount, &errMsg);

    if (rc != SQLITE_OK) {
      LOGF("테이블 조회 실패: %s\n", errMsg);
      sqlite3_free(errMsg);
      return rc;
    }

    // 첫 번째 행은 칼럼 이름이므로 스킵
    int dropIndex = colCount;

    // 각 DROP TABLE 문을 실행하여 테이블을 삭제
    for (int i = 0; i < rowCount; ++i) {
      const char* dropStatement = dropStatements[dropIndex++];
      rc = execute(dropStatement);
      if (rc != SQLITE_OK) {
        LOGF("테이블 삭제 실패: %s\n", dropStatement);
      } else {
        LOGF("테이블 삭제 성공: %s\n", dropStatement);
      }
    }

    sqlite3_free_table(dropStatements);

    return SQLITE_OK;
  }
};

DB_Manager* DB_Manager::instance = nullptr;

//-------------------------------------------------------------
#endif  //__DB_MANAGER_H__