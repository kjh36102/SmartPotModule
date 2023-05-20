#ifndef __TASK_WEB_DB_CONSOLE__
#define __TASK_WEB_DB_CONSOLE__

//----------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sqlite3.h>
#include <SPI.h>
#include <FS.h>
#include "SPIFFS.h"
#include "SD_MMC.h"
#include "SD.h"

/* You only need to format SPIFFS the first time you run a
   test or else use the SPIFFS plugin to create a partition
   https://github.com/me-no-dev/arduino-esp32fs-plugin */
#define FORMAT_SPIFFS_IF_FAILED true
#define MAX_FILE_NAME_LEN 100
#define MAX_STR_LEN 500

//--------------------------------
#define LOGKEY "TaskWebDBConsole.h"
#include "Logger.h"
// #define PRINT_STACK_USAGE true
//--------------------------------


char db_file_name[MAX_FILE_NAME_LEN] = "\0";
sqlite3 *db = NULL;
int rc;

bool first_time = false;
static int callback(void *data, int argc, char **argv, char **azColName) {
  int i;
  if (first_time) {
    LOGLN((const char *)data);
    for (i = 0; i < argc; i++) {
      if (i)
        LOG((char)'\t');
      LOGF("%s", azColName[i]);
    }
    LOGF("\n");
    first_time = false;
  }
  for (i = 0; i < argc; i++) {
    if (i)
      LOG((char)'\t');
    LOGF("%s", argv[i] ? argv[i] : "NULL");
  }
  LOGF("\n");
  return 0;
}

int db_open() {
  if (db != NULL)
    sqlite3_close(db);
  int rc = sqlite3_open(db_file_name, &db);
  if (rc) {
    LOG(F("Can't open database: "));
    LOG(sqlite3_extended_errcode(db));
    LOG(" ");
    LOGLN(sqlite3_errmsg(db));
    return rc;
  } else
    LOGLN(F("Opened database successfully"));
  return rc;
}

char *zErrMsg = 0;
const char *data = "Output:";
int db_exec(const char *sql) {
  if (db == NULL) {
    LOGLN("No database open");
    return 0;
  }
  first_time = true;
  long start = micros();
  int rc = sqlite3_exec(db, sql, callback, (void *)data, &zErrMsg);
  if (rc != SQLITE_OK) {
    LOG(F("SQL error: "));
    LOG(sqlite3_extended_errcode(db));
    LOG(" ");
    LOGLN(zErrMsg);
    sqlite3_free(zErrMsg);
  } else
    LOGLN(F("Operation done successfully"));
  LOG(F("Time taken:"));
  LOG(micros() - start);
  LOGLN(F(" us"));
  return rc;
}

int input_string(char *str, int max_len) {
  max_len--;
  int ctr = 0;
  str[ctr] = 0;
  while (str[ctr] != '\n') {
    if (Serial.available()) {
      str[ctr] = Serial.read();
      if (str[ctr] >= ' ' && str[ctr] <= '~')
        ctr++;
      if (ctr >= max_len)
        break;
    }
  }
  str[ctr] = 0;
  LOGLN(str);
  return ctr;
}

int input_num() {
  char in[20];
  int ctr = 0;
  in[ctr] = 0;
  while (in[ctr] != '\n') {
    if (Serial.available()) {
      in[ctr] = Serial.read();
      if (in[ctr] >= '0' && in[ctr] <= '9')
        ctr++;
      if (ctr >= sizeof(in))
        break;
    }
  }
  in[ctr] = 0;
  int ret = atoi(in);
  LOGLN(ret);
  return ret;
}

void listDir(fs::FS &fs, const char *dirname) {
  LOG(F("Listing directory: "));
  LOGLN(dirname);
  File root = fs.open(dirname);
  if (!root) {
    LOGLN(F("Failed to open directory"));
    return;
  }
  if (!root.isDirectory()) {
    LOGLN("Not a directory");
    return;
  }
  File file = root.openNextFile();
  while (file) {
    if (file.isDirectory()) {
      LOG(" Dir : ");
      LOGLN(file.name());
    } else {
      LOG(" File: ");
      LOG(file.name());
      LOG(" Size: ");
      LOGLN(file.size());
    }
    file = root.openNextFile();
  }
}

void renameFile(fs::FS &fs, const char *path1, const char *path2) {
  LOGF("Renaming file %s to %s\n", path1, path2);
  if (fs.rename(path1, path2)) {
    LOGLN(F("File renamed"));
  } else {
    LOGLN(F("Rename failed"));
  }
}

void deleteFile(fs::FS &fs, const char *path) {
  LOGF("Deleting file: %s\n", path);
  if (fs.remove(path)) {
    LOGLN(F("File deleted"));
  } else {
    LOGLN(F("Delete failed"));
  }
}

enum { CHOICE_OPEN_DB = 1,
       CHOICE_EXEC_SQL,
       CHOICE_EXEC_MULTI_SQL,
       CHOICE_CLOSE_DB,
       CHOICE_LIST_FOLDER,
       CHOICE_RENAME_FILE,
       CHOICE_DELETE_FILE,
       CHOICE_SHOW_FREE_MEM };
int askChoice() {
  LOGLN("");
  LOGLN(F("Welcome to SQLite console!!"));
  LOGLN(F("---------------------------"));
  LOGLN("");
  LOG(F("Database file: "));
  LOGLN(db_file_name);
  LOGLN("");
  LOGLN(F("1. Open database"));
  LOGLN(F("2. Execute SQL"));
  LOGLN(F("3. Execute Multiple SQL"));
  LOGLN(F("4. Close database"));
  LOGLN(F("5. List folder contents"));
  LOGLN(F("6. Rename file"));
  LOGLN(F("7. Delete file"));
  LOGLN(F("8. Show free memory"));
  LOGLN("");
  LOG(F("Enter choice: "));
  return input_num();
}

void displayPrompt(const char *title) {
  LOGLN(F("(prefix /spiffs/ or /sd/ or /sdcard/ for"));
  LOGLN(F(" SPIFFS or SD_SPI or SD_MMC respectively)"));
  LOG(F("Enter "));
  LOGLN(title);
}

const char *prefixSPIFFS = "/spiffs/";
const char *prefixSD_SPI = "/sd/";
const char *prefixSD_MMC = "/sdcard/";
fs::FS *ascertainFS(const char *str, int *prefix_len) {
  if (strstr(str, prefixSPIFFS) == str) {
    *prefix_len = strlen(prefixSPIFFS) - 1;
    return &SPIFFS;
  }
  if (strstr(str, prefixSD_SPI) == str) {
    *prefix_len = strlen(prefixSD_SPI) - 1;
    return &SD;
  }
  if (strstr(str, prefixSD_MMC) == str) {
    *prefix_len = strlen(prefixSD_MMC) - 1;
    return &SD_MMC;
  }
  return NULL;
}

void initTaskWebDBConsole() {
  if (!SPIFFS.begin(FORMAT_SPIFFS_IF_FAILED)) {
    LOGLN(F("Failed to mount file Serial"));
    return;
  }
  SPI.begin();
  SD_MMC.begin();
  SD.begin();
  sqlite3_initialize();
}

char str[MAX_STR_LEN];
void tWebDBConsole(void *taskParams) {
  initTaskWebDBConsole();
  for (;;) {

    int choice = askChoice();
    switch (choice) {
      case CHOICE_OPEN_DB:
        displayPrompt("file name: ");
        input_string(str, MAX_FILE_NAME_LEN);
        if (str[0] != 0) {
          strncpy(db_file_name, str, MAX_FILE_NAME_LEN);
          db_open();
        }
        break;
      case CHOICE_EXEC_SQL:
        LOG(F("Enter SQL (max "));
        LOG(MAX_STR_LEN);
        LOGLN(F(" characters):"));
        input_string(str, MAX_STR_LEN);
        if (str[0] != 0)
          db_exec(str);
        break;
      case CHOICE_EXEC_MULTI_SQL:
        LOGLN(F("(Copy paste may not always work due to limited serial buffer)"));
        LOGLN(F("Keep entering SQL, empty to stop:"));
        do {
          input_string(str, MAX_STR_LEN);
          if (str[0] != 0)
            db_exec(str);
        } while (str[0] != 0);
        break;
      case CHOICE_CLOSE_DB:
        if (db_file_name[0] != 0) {
          db_file_name[0] = 0;
          sqlite3_close(db);
        }
        break;
      case CHOICE_LIST_FOLDER:
      case CHOICE_RENAME_FILE:
      case CHOICE_DELETE_FILE:
        fs::FS *fs;
        displayPrompt("path: ");
        input_string(str, MAX_STR_LEN);
        if (str[0] != 0) {
          int fs_prefix_len = 0;
          fs = ascertainFS(str, &fs_prefix_len);
          if (fs != NULL) {
            switch (choice) {
              case CHOICE_LIST_FOLDER:
                listDir(*fs, str + fs_prefix_len);
                break;
              case CHOICE_RENAME_FILE:
                char str1[MAX_FILE_NAME_LEN];
                displayPrompt("path to rename as: ");
                input_string(str1, MAX_STR_LEN);
                if (str1[0] != 0)
                  renameFile(*fs, str + fs_prefix_len, str1 + fs_prefix_len);
                break;
              case CHOICE_DELETE_FILE:
                deleteFile(*fs, str + fs_prefix_len);
                break;
            }
          }
        }
        break;
      case CHOICE_SHOW_FREE_MEM:
        LOGF("\nHeap size: %d\n", ESP.getHeapSize());
        LOGF("Free Heap: %d\n", esp_get_free_heap_size());
        LOGF("Min Free Heap: %d\n", esp_get_minimum_free_heap_size());
        LOGF("Largest Free block: %d\n", heap_caps_get_largest_free_block(MALLOC_CAP_8BIT));
        break;
      default:
        LOGLN(F("Invalid choice. Try again."));
    }

    vTaskDelay(1);
  }
}



#endif  // __TASK_WEB_DB_CONSOLE__