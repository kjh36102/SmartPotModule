#ifndef __TASK_DB_MANAGER_H__
#define __TASK_DB_MANAGER_H__

//-------------------------------------------------------------
#include <Arduino.h>

#include "DB_Manager.h"
#include "PowerMonitor.h"

//-------------------------------------------------------------
#define LOGKEY "TaskDBManager.h"
#include "Logger.h"
//-------------------------------------------------------------

// sqlite3 *DB;

// #define JSON_DOCUMENT_SIZE 1024
// char *DBErrMsg = 0;

// //쿼리 실행시 인자로 쿼리결과를 받고 처리하는 콜백함수
// const char *data = "Query results:";
// char global_result[JSON_DOCUMENT_SIZE];  // 전역 결과 문자열
// bool stateTransDB = false;


// static int callbackOnExecuteDB(void *data, int argc, char **argv, char **azColName) {
//   StaticJsonDocument<JSON_DOCUMENT_SIZE> doc;
//   for (int i = 0; i < argc; i++) {
//     if (argv[i]) {
//       doc[azColName[i]] = argv[i];
//     } else {
//       doc[azColName[i]] = "NULL";
//     }
//   }

//   // JSON 형식으로 변환
//   char buffer[JSON_DOCUMENT_SIZE];
//   serializeJson(doc, buffer);

//   // 결과를 전역 변수에 추가
//   strcpy(global_result, buffer);
//   stateTransDB = false;
//   return 0;
// }

// bool waitTransaction(){
//   do{
//     delay(10);
//   }while(stateTransDB);
// }


// bool stateDBOpen = false;
// int openDB() {
//   int rc = sqlite3_open("/sd/data.db", &DB);

//   if (rc) {
//     LOG("DB 열기 실패: ");
//     LOGLN(sqlite3_errmsg(DB));
//   } else {
//     stateDBOpen = true;
//     LOGLN("DB 열기 성공!");
//   }

//   return rc;
// }

// void closeDB() {
//   for (byte i = 0; i < 10; i++) {
//     digitalWrite(5, 1);
//     delay(50);
//     digitalWrite(5, 0);
//     delay(50);
//   }

//   sqlite3_close(DB);
//   sqlite3_free(DBErrMsg);
//   stateDBOpen = false;

//   digitalWrite(5, 1);
//   delay(1000);
//   digitalWrite(5, 0);
// }


// int executeSqlHelper(const char *sql) {
//   if (!stateDBOpen) return 0;

//   waitTransaction();
//   stateTransDB = true;

//   LOGF("SQL 실행: %s\n", sql);

//   unsigned long start = millis();

//   int rc = sqlite3_exec(DB, sql, callbackOnExecuteDB, (void *)data, &DBErrMsg);

//   if (rc != SQLITE_OK) {
//     LOGF("SQL 오류: %s\n", DBErrMsg);
//     sqlite3_free(DBErrMsg);
//   } else {
//     LOGLN("SQL 실행됨");
//   }

//   Serial.print(F("\t소요 시간(ms):"));
//   Serial.println(millis() - start);
//   return rc;
// }

// int executeSql(const char *sql) {
//   return executeSqlHelper(sql);
// }

// int executeSql(const __FlashStringHelper *sql) {
//   const char *sqlPtr = reinterpret_cast<const char *>(sql);
//   return executeSqlHelper(sqlPtr);
// }


// void createTablesIfNotExists() {

//   if (!stateDBOpen) return;

//   executeSql(F("CREATE TABLE IF NOT EXISTS soil_data(id INTEGER PRIMARY KEY, tm REAL, hm REAL, n REAL, p REAL, k REAL, ph REAL, ec INTEGER, lt INTEGER, ts TEXT DEFAULT(datetime('now', 'localtime')))"));
//   executeSql(F("CREATE TABLE IF NOT EXISTS wifi_info(id INTEGER PRIMARY KEY, ssid_ap TEXT, pw_ap TEXT, ssid_sta TEXT, pw_sta TEXT)"));
//   executeSql(F("CREATE TABLE IF NOT EXISTS plant_manage(id INTEGER PRIMARY KEY, w_auto INTEGER, l_auto INTEGER, w_on INTEGER, l_on INTEGER)"));
//   executeSql(F("CREATE TABLE IF NOT EXISTS manage_auto(id INTEGER PRIMARY KEY, hm REAL, th REAL, lt INTEGER, dr INTEGER)"));
//   executeSql(F("CREATE TABLE IF NOT EXISTS manage_water(id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, wt INTEGER, no TEXT)"));
//   executeSql(F("CREATE TABLE IF NOT EXISTS manage_light(id INTEGER PRIMARY KEY, ud INTEGER, st TEXT, ls INTEGER, no TEXT)"));

//   //기본데이터 넣기
//   // INSERT INTO soil_data(id, tm, hm, n, p, k, ph, ec, lt) values(0, 0, 0, 0, 0, 0, 0, 0, 0);
//   // INSERT INTO wifi_info(id, ssid_ap, pw_ap, ssid_sta, pw_sta) values(0, 'SmartPotModule', NULL, NULL, NULL);
//   // INSERT INTO plant_manage values(0, 0, 0, 0, 0);
//   // INSERT INTO manage_auto values(0, NULL, NULL, NULL, NULL);

//   LOGLN("테이블 생성 완료");
// }

// void dropAllTables() {
//   executeSql(F("DROP TABLE IF EXISTS soil_data"));
//   executeSql(F("DROP TABLE IF EXISTS wifi_info"));
//   executeSql(F("DROP TABLE IF EXISTS plant_manage"));
//   executeSql(F("DROP TABLE IF EXISTS manage_auto"));
//   executeSql(F("DROP TABLE IF EXISTS manage_water"));
//   executeSql(F("DROP TABLE IF EXISTS manage_light"));

//   LOGLN("테이블 제거 완료");
// }

// bool stateDBPrepared = false;
// void initTaskDBManager() {
//   DBErrMsg = 0;

//   SPI.begin();
//   SD.begin();

//   sqlite3_initialize();

//   openDB();

//   //종료함수로 db닫기 함수를 등록
//   appendShutdownProcess(closeDB);

//   //테이블 생성하는 sql문들 실행
//   createTablesIfNotExists();

//   stateDBPrepared = true;
// }

// void tTestDBManager(void *taskParams) {
//   // initTaskDBManager();

//   DB_Manager dbManager = new DB_Manager(1024);

//   while (true) {
//     if (Serial.available()) {
//       String msg = Serial.readStringUntil('\n');

//       if (msg.equals("create all tables")) {
//         // createTablesIfNotExists();
//       } else if (msg.equals("drop all tables")) {
//         // dropAllTables();
//       } else if (msg.equals("setup")) {
//         connectPhase = ConnectPhase::SETUP;
//         initNetwork(true);
//       } else if (msg.equals("close")) {
//         dbManager.close();
//       } else if (msg.equals("open")) {
//         dbManager.open("data");
//       } else if (msg.equals("free")) {
//         LOGF("FreeHeapMemory: %u\n", esp_get_free_heap_size());
//       } else {
//         // executeSql(msg.c_str());

//         // LOGLN(global_result);

//         dbManager.execute(msg.c_str());
//         LOGLN(dbManager.getResult());
//       }
//     }

//     vTaskDelay(200);
//   }
// }



//-------------------------------------------------------------
#endif  //__TASK_DB_MANAGER_H__