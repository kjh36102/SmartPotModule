#ifndef __TIME_UPDATER_H__
#define __TIME_UPDATER_H__

//-------------------------------------------------------------
#include <Arduino.h>

#include <WiFi.h>
#include <time.h>
#include "MultitaskRTOS.h"
#include "ArduinoJson.h"

//-------------------------------------------------------------
#define LOGKEY "TimeUpdater.h"
#include "Logger.h"
//-------------------------------------------------------------

class TimeUpdater {
private:
  static TimeUpdater* instance;

  TimeUpdater() { }

public:
  static TimeUpdater& getInstance() {
    if (!instance) {
      instance = new TimeUpdater();
      createAndRunTask(tTimeRenewer, "tTimeRenewer");
    }
    return *instance;
  }

  bool isConnectedToInternet() {
    WiFiClient client;

    bool connState = client.connect("www.google.com", 80);
    client.stop();
    return connState;
  }

  static void tTimeRenewer(void* taskParams) {
    LOGLN("TimeRenewer Started..");
    for (;;) {
      TimeUpdater& timeUpdater = TimeUpdater::getInstance();
      timeUpdater.updateCurrentTime();

      vTaskDelay(300000); //5분마다 시간 업데이트
    }
  }


  bool updateCurrentTime() {

    if (!isConnectedToInternet()) {
      LOGLN("Not connected to internet. Cannot update time.");
      return false;
    }

    configTime(9 * 3600, 0, "pool.ntp.org");  // 9 * 3600은 한국시간

    while (!time(nullptr)) {  //시간 업데이트 대기
      delay(10);
    }

    LOGLN("Current time updated.");

    return true;
  }

  String getCurrentTime() {
    time_t now = time(nullptr);
    struct tm* timeinfo;
    char buffer[20];

    timeinfo = localtime(&now);
    strftime(buffer, sizeof(buffer), "%Y-%m-%d %H:%M:%S", timeinfo);

    return String(buffer);
  }

  String getNextOperationTime(int unitDay, String setTime, bool returnNearest) {
    LOGLN("getNextOperationTime Received Args:");
    LOGF("\tunitDay: %d\n\tsetTime: %s\n\treturnNearest: %d\n", unitDay, setTime, (returnNearest) ? 1 : 0);

    time_t now = time(nullptr);
    struct tm* timeinfo;
    char buffer[20];
    String operationTime;

    timeinfo = localtime(&now);
    int year = timeinfo->tm_year + 1900;  // tm_year is the number of years since 1900
    int month = timeinfo->tm_mon + 1;     // tm_mon is the month of the year from 0 (Jan) to 11 (Dec)
    int day = timeinfo->tm_mday;

    if (returnNearest) {
      // Parse hour and minute from setTime
      int hour = setTime.substring(0, 2).toInt();
      int min = setTime.substring(3, 5).toInt();
      if (timeinfo->tm_hour < hour || (timeinfo->tm_hour == hour && timeinfo->tm_min < min)) {
        // If current time of the day is before the operation time, return the same day's operation time
        sprintf(buffer, "%04d-%02d-%02d %s", year, month, day, setTime.c_str());
        operationTime = String(buffer);
      } else {
        // If current time of the day is after the operation time, return the next day's operation time
        time_t nextDay = now + (24 * 60 * 60);  // Add one day in seconds
        timeinfo = localtime(&nextDay);
        sprintf(buffer, "%04d-%02d-%02d %s", timeinfo->tm_year + 1900, timeinfo->tm_mon + 1, timeinfo->tm_mday, setTime.c_str());
        operationTime = String(buffer);
      }
    } else {
      // If returnNearest is false, calculate the operation time after unitDay period
      time_t nextOperation = now + (unitDay * 24 * 60 * 60);
      timeinfo = localtime(&nextOperation);
      sprintf(buffer, "%04d-%02d-%02d %s", timeinfo->tm_year + 1900, timeinfo->tm_mon + 1, timeinfo->tm_mday, setTime.c_str());
      operationTime = String(buffer);
    }
    return operationTime;
  }

  int getCurrentSeconds() {
    String currentTime = getCurrentTime();
    int secondIndex = currentTime.lastIndexOf(':') + 1;
    String secondsString = currentTime.substring(secondIndex);
    return secondsString.toInt();
  }

  String getCurrentTimeNoSecond() {
    time_t now = time(nullptr);
    struct tm* timeinfo;
    char buffer[17];

    timeinfo = localtime(&now);
    strftime(buffer, sizeof(buffer), "%Y-%m-%d %H:%M", timeinfo);

    return String(buffer);
  }

  bool isValidSetTimeFormat(const String& timeString) {
    // Check if the timeString is in the format "%H:%M"
    if (timeString.length() != 5) {
      return false;
    }

    // Extract the hour and minute components
    int hour = timeString.substring(0, 2).toInt();
    int minute = timeString.substring(3, 5).toInt();

    // Validate the hour and minute values
    if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
      return false;
    }

    return true;
  }

  String getReplaceOldTime(const char* oldTimestamp) {
    // "1970-01-01 00:00:00" 형태의 timestamp를 time_t 형태로 변환
    struct tm tm;
    strptime(oldTimestamp, "%Y-%m-%d %H:%M:%S", &tm);
    time_t oldTime = mktime(&tm);

    // oldTime이 "2020-01-01 00:00:00" 이전인지 확인
    struct tm time2020;
    strptime("2020-01-01 00:00:00", "%Y-%m-%d %H:%M:%S", &time2020);
    time_t time2020_t = mktime(&time2020);

    if (oldTime >= time2020_t) {
      // oldTime이 "2020-01-01 00:00:00" 이후라면 원래의 타임스탬프를 그대로 반환
      return String(oldTimestamp);
    }

    // 부팅 후 경과한 시간 계산
    time_t timeElapsedSinceBoot = oldTime - (24 * 60 * 60);  // 1970-01-01 00:00:00 에서부터의 시간

    // 현재 시간을 계산
    time_t currentTime = time(nullptr);

    // 부팅된 시간 계산
    time_t bootTime = currentTime - timeElapsedSinceBoot;

    // 오래된 timestamp를 현재의 절대적 시간으로 업데이트
    time_t updatedTime = bootTime + (oldTime - (24 * 60 * 60));

    // time_t를 string으로 변환
    char buffer[26];
    struct tm* timeinfo = localtime(&updatedTime);
    strftime(buffer, 26, "%Y-%m-%d %H:%M:%S", timeinfo);

    return String(buffer);
  }
};

TimeUpdater* TimeUpdater::instance = nullptr;


//-------------------------------------------------------------
#endif  //__TIME_UPDATER_H__