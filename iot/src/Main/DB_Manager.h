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
        
        if(sdInitialized){
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
};

DB_Manager* DB_Manager::instance = nullptr;

//-------------------------------------------------------------
#endif  //__DB_MANAGER_H__