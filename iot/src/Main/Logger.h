#ifndef __LOGGER_H__
#define __LOGGER_H__
//-------------------------------------------------------------

/*
각 파일마다 로그를 켜고 끌수있게 해주는 라이브러리

각 파일의 가장 "마지막" 전처리문으로써 아래와 같이 선언해야함
#define LOGKEY "TheFilename.h"
#include "Logger.h"

그리고 setup함수에서 아래와 같이 호출해야함
enableLogging("TheFilename.h")

이렇게 하면 TheFilename.h 내에서 사용한 로그매크로만 작동함
*/
#include <Arduino.h>
#include <map>

#define LOG(VAL) log(LOGKEY, VAL)
#define LOGLN(VAL) logln(LOGKEY, VAL)
#define LOGF(FORMAT, ...) logf(LOGKEY, FORMAT, ##__VA_ARGS__)

std::map<const char*, bool> loggingID;

void enableLogging(const char* key) {
  loggingID[key] = true;
}

void disableLogging(const char* key) {
  loggingID.erase(key);
}

template<typename T>
void log(const char* key, T value) {
  if (loggingID.count(key) > 0) {
    Serial.print(value);
  }
}

template<typename T>
void logln(const char* key, T value) {
  if (loggingID.count(key) > 0) {
    Serial.println(value);
  }
}

template<typename... Args>
void logf(const char* key, const char* format, Args... args) {
  if (loggingID.count(key) > 0) {
    Serial.printf(format, args...);
  }
}


//-------------------------------------------------------------
#endif  // __LOGGER_H__
