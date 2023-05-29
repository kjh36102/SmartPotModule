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

char *DBErrMsg = 0;

//쿼리 실행시 인자로 쿼리결과를 받고 처리하는 콜백함수
const char *data = "Query results:";
// static int callbackOnExecuteDB(void *data, int argc, char **argv, char **azColName) {
//   int i;
//   LOGF("Query results: \n");
//   for (i = 0; i < argc; i++) {
//     LOGF("\t%s", azColName[i]);
//   }
//   LOGLN("");
//   for (i = 0; i < argc; i++) {
//     LOGF("\t%s", argv[i] ? argv[i] : "NULL");
//   }
//   LOGLN("");
//   return 0;
// }
#define MAX_RESULT_SIZE 300
#define MAX_COLUMN_SIZE 50
char global_result[MAX_RESULT_SIZE];  // 전역 결과 문자열


static int callbackOnExecuteDB(void *data, int argc, char **argv, char **azColName) {

  int i;
  int global_result_pos = 0;  // 현재 결과의 위치

  for (i = 0; i < argc; i++) {
    // 데이터를 전역 결과에 추가
    char column_data[MAX_COLUMN_SIZE];
    sprintf(column_data, "%s", argv[i] ? argv[i] : "NULL");

    // '|'와 ','로 구분
    if (i != argc - 1)
      strcat(column_data, "\t");
    else
      strcat(column_data, "\n");

    // 결과를 전역 변수에 추가
    strcpy(global_result + global_result_pos, column_data);
    global_result_pos += strlen(column_data);
  }

  // global_result[global_result_pos] = '\0';
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
  for (byte i = 0; i < 10; i++) {
    digitalWrite(5, 1);
    delay(50);
    digitalWrite(5, 0);
    delay(50);
  }

  sqlite3_close(DB);
  sqlite3_free(DBErrMsg);
  stateDBOpen = false;

  digitalWrite(5, 1);
  delay(1000);
  digitalWrite(5, 0);
}


int executeSqlHelper(const char *sql) {
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

int executeSql(const char *sql) {
  return executeSqlHelper(sql);
}

int executeSql(const __FlashStringHelper *sql) {
  const char *sqlPtr = reinterpret_cast<const char *>(sql);
  return executeSqlHelper(sqlPtr);
}


void createTablesIfNotExists() {

  if (!stateDBOpen) return;

  executeSql(F("CREATE TABLE IF NOT EXISTS soil_data(id INTEGER PRIMARY KEY, tm REAL, hm REAL, n REAL, p REAL, k REAL, ph REAL, ec INTEGER, lt INTEGER, ts TEXT DEFAULT(datetime('now', 'localtime')))"));
  executeSql(F("CREATE TABLE IF NOT EXISTS wifi_info(id INTEGER PRIMARY KEY, ssid_ap TEXT, pw_ap TEXT, ssid_sta TEXT, pw_sta TEXT)"));
  executeSql(F("CREATE TABLE IF NOT EXISTS plant_manage(id INTEGER PRIMARY KEY, w_auto INTEGER, l_auto INTEGER, w_on INTEGER, l_on INTEGER)"));
  executeSql(F("CREATE TABLE IF NOT EXISTS manage_auto(id INTEGER PRIMARY KEY, hm REAL, th REAL, lt INTEGER, dr INTEGER)"));
  executeSql(F("CREATE TABLE IF NOT EXISTS manage_water(id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, wt INTEGER, no TEXT)"));
  executeSql(F("CREATE TABLE IF NOT EXISTS manage_light(id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, ls INTEGER, no TEXT)"));

  //기본데이터 넣기
  // INSERT INTO soil_data(id, tm, hm, n, p, k, ph, ec, lt) values(0, 0, 0, 0, 0, 0, 0, 0, 0);
  // INSERT INTO wifi_info(id, ssid_ap, pw_ap, ssid_sta, pw_sta) values(0, 'SmartPotModule', NULL, NULL, NULL);
  // INSERT INTO plant_manage values(0, 0, 0, 0, 0);
  // INSERT INTO manage_auto values(0, NULL, NULL, NULL, NULL);

  LOGLN("테이블 생성 완료");
}

void dropAllTables() {
  executeSql(F("DROP TABLE IF EXISTS soil_data"));
  executeSql(F("DROP TABLE IF EXISTS wifi_info"));
  executeSql(F("DROP TABLE IF EXISTS plant_manage"));
  executeSql(F("DROP TABLE IF EXISTS manage_auto"));
  executeSql(F("DROP TABLE IF EXISTS manage_water"));
  executeSql(F("DROP TABLE IF EXISTS manage_light"));

  LOGLN("테이블 제거 완료");
}

bool stateDBPrepared = false;
void initTaskDBManager() {
  DBErrMsg = 0;

  SPI.begin();
  SD.begin();

  sqlite3_initialize();

  openDB();

  //종료함수로 db닫기 함수를 등록
  appendShutdownProcess(closeDB);

  //테이블 생성하는 sql문들 실행
  createTablesIfNotExists();

  stateDBPrepared = true;
}

void tTestDBManager(void *taskParams) {
  initTaskDBManager();

  while (true) {
    if (Serial.available()) {
      String msg = Serial.readStringUntil('\n');

      // if (msg.equals("getSoilDataCount")) {
      //   // Query를 실행합니다.
      //   String ***result = NULL;
      //   int record_count = executeSqlHelper("SELECT count(*) FROM soil_data", result);

      //   // 결과를 출력합니다.
      //   for (int i = 0; i < record_count; i++) {
      //     Serial.printf("i=%d\n", i);
      //     for (int j = 0; j < 1; j++) {
      //       Serial.printf("j=%d\n", j);
      //       Serial.println(*result[i][j]);
      //     }
      //   }

      //   // 메모리를 해제합니다.
      //   freeSQLResult(result, record_count);
      // } else if (msg.equals("getSoilData")) {
      //   // Query를 실행합니다.
      //   String ***result = NULL;
      //   int record_count = executeSqlHelper("SELECT * FROM soil_data where id=0", result);

      //   // 결과를 출력합니다.
      //   for (int i = 0; i < record_count; i++) {
      //     Serial.printf("i=%d\n", i);
      //     for (int j = 0; j < 9; j++) {
      //       Serial.printf("j=%d\n", j);
      //       Serial.println(*result[i][j]);
      //     }
      //   }

      //   // 메모리를 해제합니다.
      //   freeSQLResult(result, record_count);

      // } else
      if (msg.equals("create all tables")) {
        createTablesIfNotExists();
      } else if (msg.equals("drop all tables")) {
        dropAllTables();
      } else if (msg.equals("setup")) {
        connectPhase = ConnectPhase::SETUP;
        initNetwork(true);
      } else if (msg.equals("close")) {
        closeDB();
      } else if (msg.equals("open")) {
        openDB();
      } else if (msg.equals("free")) {
        LOGF("FreeHeapMemory: %u\n", esp_get_free_heap_size());
      } else {
        executeSql(msg.c_str());

        LOGLN(global_result);
      }
    }

    vTaskDelay(200);
  }
}



//-------------------------------------------------------------
#endif  //__TASK_DB_MANAGER_H__