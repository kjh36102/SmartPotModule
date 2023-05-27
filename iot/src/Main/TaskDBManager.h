#ifndef __TASK_DB_MANAGER_H__
#define __TASK_DB_MANAGER_H__

//-------------------------------------------------------------
#include <Arduino.h>

#include <stdio.h>
#include <stdlib.h>
#include <sqlite3.h>
#include <SPI.h>
#include <FS.h>
#include "SD.h"

#include "TaskMonitorExtPwr.h"

//-------------------------------------------------------------
#define LOGKEY "TaskDBManager.h"
#include "Logger.h"
//-------------------------------------------------------------

sqlite3 *DB;

//쿼리 실행시 인자로 쿼리결과를 받고 처리하는 콜백함수
const char *data = "Query results:";
static int callbackOnExecuteDB(void *data, int argc, char **argv, char **azColName) {
  int i;
  LOGF("Query results: \n");
  for (i = 0; i < argc; i++) {
    LOGF("\t%s", azColName[i]);
  }
  LOGLN("");
  for (i = 0; i < argc; i++) {
    LOGF("\t%s", argv[i] ? argv[i] : "NULL");
  }
  LOGLN("");
  return 0;
}

bool stateDBOpen = false;
int openDB() {
  int rc = sqlite3_open("/sd/data.db", &DB);

  if (rc) {
    LOG("DB 열기 실패: ");
    LOGLN(sqlite3_errmsg(DB));
  } else {
    stateDBOpen = true;
    LOGLN("DB 열기 성공!");
  }

  return rc;
}

void closeDB() {
  sqlite3_close(DB);
  LOGLN("DB 닫기 완료");
  stateDBOpen = false;

  pinMode(27, OUTPUT);
  digitalWrite(26, 1);
  delay(1000);
  digitalWrite(26, 0);
}

char *DBErrMsg = 0;
int executeSql(const char *sql) {
  if (!stateDBOpen) return 0;

  LOGF("SQL 실행: %s\n", sql);

  unsigned long start = millis();

  int rc = sqlite3_exec(DB, sql, callbackOnExecuteDB, (void *)data, &DBErrMsg);

  if (rc != SQLITE_OK) {
    LOGF("SQL 오류: %s\n", DBErrMsg);
    sqlite3_free(DBErrMsg);
  } else {
    LOGLN("SQL 실행됨");
  }

  Serial.print(F("\t소요 시간(ms):"));
  Serial.println(millis() - start);
  return rc;
}

void createTablesIfNotExists(){
  
}

void initTaskDBManager() {
  DBErrMsg = 0;

  SPI.begin();
  SD.begin();

  sqlite3_initialize();

  openDB();

  //종료함수로 db닫기 함수를 등록
  appendShutdownProcess(closeDB);
}

void tTestDBManager(void *taskParams) {
  initTaskDBManager();

  while (true) {
    if (Serial.available()) {
      String msg = Serial.readStringUntil('\n');
      
      if(msg.equals("setup")){
        connectPhase = ConnectPhase::SETUP;
        initNetwork(true);
      }else if (msg.equals("close")) {
        closeDB();
      } else if (msg.equals("open")) {
        openDB();
      } else if (msg.equals("free")) {
        LOGF("FreeHeapMemory: %u\n", esp_get_free_heap_size());
      } else {
        executeSql(msg.c_str());
      }
    }

    vTaskDelay(200);
  }
}



//-------------------------------------------------------------
#endif  //__TASK_DB_MANAGER_H__