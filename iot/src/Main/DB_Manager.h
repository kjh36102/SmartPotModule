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

class DB_Manager {
private:
  sqlite3* DB;
  char* DBErrMsg = 0;
  int bufferSize;
  const char* dbName;   // 사용 중인 데이터베이스 이름을 저장
  char* global_result;  // 전역 결과 문자열
  bool stateTransDB = false;
  bool stateOpenDB = false;
  static bool sdInitialized;  // SD 카드 초기화 상태

  static int callbackOnExecuteDB(void* instance, int argc, char** argv, char** azColName) {
    DB_Manager* self = reinterpret_cast<DB_Manager*>(instance);

    char* result = new char[self->bufferSize];
    result[0] = '\0';  // Initialize result string

    for (int i = 0; i < argc; i++) {
      strcat(result, azColName[i]);
      strcat(result, ": ");
      if (argv[i]) {
        strcat(result, argv[i]);
      } else {
        strcat(result, "NULL");
      }
      if (i < argc - 1) {
        strcat(result, ", ");
      }
    }

    // 결과를 전역 변수에 추가
    strcpy(self->global_result, result);

    // result 문자열 메모리 해제
    delete[] result;

    self->stateTransDB = false;
    return 0;
  }

  int executeSqlHelper(const char* sql) {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 SQL문 실행 불가능");
      return 0;
    }

    waitTransaction();  //이미 트랜잭션 중이라면 대기
    stateTransDB = true;

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

  bool waitTransaction() {
    LOG("Waiting Transaction");
    do {
      delay(10);
      LOG(".");
    } while (stateTransDB);
    LOG("..Done!");
  }

public:
  DB_Manager(int bufferSize)
    : bufferSize(bufferSize) {
    // 동적 메모리 할당
    global_result = new char[bufferSize];

    //SD카드 초기화
    if (!DB_Manager::sdInitialized) {
      if (!SD.begin()) {
        LOGLN("SD 카드 초기화 실패");
      } else {
        LOGLN("SD 카드 초기화 성공");
        DB_Manager::sdInitialized = true;
      }
    }
  }

  ~DB_Manager() {
    // 동적 메모리 해제
    delete[] global_result;
  }

  DB_Manager(const DB_Manager&) = delete;
  DB_Manager& operator=(const DB_Manager&) = delete;

  bool open(String dbName) {
    if (stateOpenDB) {
      LOGLN("DB가 이미 열려있음");
      return false;
    }

    this->dbName = dbName.c_str();

    int rc = sqlite3_open(("/sd/" + dbName + ".db").c_str(), &DB);

    if (rc) {
      LOGF("DB '%s' 열기 실패: ", this->dbName);
      LOGLN(sqlite3_errmsg(DB));
      return false;
    } else {
      stateOpenDB = true;
      LOGLN("DB 열기 성공!");
      return true;
    }

    return rc;
  }

  bool close() {
    if (!stateOpenDB) {
      LOGLN("DB가 열려있지 않아 닫을 수 없음");
      return false;
    }

    int rc = sqlite3_close(DB);

    if (rc) {
      LOGF("DB '%s' 닫기 실패: ", this->dbName);
      LOGLN(sqlite3_errmsg(DB));
      return false;
    } else {
      stateOpenDB = false;
      LOGLN("DB 닫기 성공!");
      return true;
    }
  }

  char* getResult() {
    waitTransaction();
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

bool DB_Manager::sdInitialized = false;



//-------------------------------------------------------------
#endif  //__DB_MANAGER_H__