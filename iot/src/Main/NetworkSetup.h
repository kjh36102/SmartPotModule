// #include "IPAddress.h"
#ifndef __NETWORK_SETUP_H__
#define __NETWORK_SETUP_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>

//-------------------------------------------------------------
#define LOGKEY "NetworkSetup.h"
#include "Logger.h"
//-------------------------------------------------------------

#define AP_PORT 12344
#define STA_PORT 12345

const char* AP_SSID = "SmartPotModule";
const char* AP_PW = "";
String STA_SSID = "";
String STA_PW = "";
IPAddress smartphoneIP = IPAddress(0, 0, 0, 0);

WebServer serverAP(AP_PORT);
WebServer serverSTA(STA_PORT);

enum class ConnectPhase {
  INITIAL,
  SETUP,
  STA_INFO,
  STA_CONNECTED,
  UDP_BROADCAST,
  UDP_ACK,
  COMPLETE,
  IDLE,
  REFRESH
};

ConnectPhase connectPhase = ConnectPhase::INITIAL;

const char* HTTP_MIME = "text/html; charset=UTF-8";

bool connectStationAP(String SSID, String PW, unsigned long timeout = 30000) {

  //STA 연결 시도
  unsigned long staConnStartTime = millis();
  WiFi.begin(SSID.c_str(), PW.c_str());

  bool ret = true;
  LOG(F("외부 네트워크 연결중.."));
  while (WiFi.status() != WL_CONNECTED) {

    if (millis() - staConnStartTime >= timeout) {
      LOGLN(F("\n\t연결시간 초과!"));
      ret = false;
      break;
    }
    LOG(".");
    delay(500);
  }

  if (ret) {
    LOGLN(F("\n\t연결됨!"));
    LOGF("\tSSID: %s\n\tPW: %s\n\t외부IP: %s\n", SSID.c_str(), PW.c_str(), WiFi.localIP().toString().c_str());
  }



  return ret;
}

void initNetwork(bool clearAll = false) {

  if (clearAll) {
    STA_SSID = "";
    STA_PW = "";
    smartphoneIP = IPAddress(0, 0, 0, 0);
    connectPhase = ConnectPhase::SETUP;
  } else {
    //db로부터 데이터 로드하기
  }

  //현재상태저장
  ConnectPhase tempPhase = connectPhase;
  connectPhase = ConnectPhase::REFRESH;

  //와이파이와 서버  끄기
  serverAP.stop();
  serverSTA.stop();
  WiFi.softAPdisconnect(true);
  WiFi.disconnect();

  LOG(F("와이파이 연결 해제중.."));
  while (WiFi.status() == WL_CONNECTED && WiFi.status() != WL_DISCONNECTED) {  //연결해제 기다리기
    LOG(".");
    delay(100);
  }
  LOGLN(F("\n\t연결 해제됨!"));

  bool staConnFailed = false;
  if (STA_SSID.length() != 0) {  //외부네트워크 정보 있음
    if (connectStationAP(STA_SSID, STA_PW)) {
      serverSTA.begin();
      delay(1000);
      connectPhase = tempPhase;
      return;
    } else {
      LOGLN(F("기존 정보로 연결에 실패함, 초기화"));
      STA_SSID = "";
      STA_PW = "";
    }
  }

  if (STA_SSID.length() == 0) {
    LOGLN(F("외부 네트워크 정보가 없음"));
  }

  delay(1000);
  connectPhase = tempPhase;

  if (connectPhase == ConnectPhase::SETUP) {
    //AP 실행
    if (strcmp(AP_PW, "") == 0)
      WiFi.softAP(AP_SSID);
    else WiFi.softAP(AP_SSID, AP_PW);
    LOGLN(F("핫스팟 실행됨"));

    //AP 서버 실행
    serverAP.begin();
    LOGF("\t핫스팟IP: %s\n", WiFi.softAPIP().toString().c_str());
  }
}

/*
이 함수는 query로 넘어온 인자들의 존재와 빈 문자열인지의 조건을 검사해서 bool값을 리턴한다.
*/
bool hasValidArg(WebServer& server, const char* argName, bool notNull = true) {
  if (server.hasArg(argName)) {
    String value = server.arg(argName);
    LOGF("\tReceived Arg ( %s ): %s\n", argName, value.c_str());

    if (notNull)
      return (value.length() > 0) ? true : false;
    else return true;
  }
  return false;
}

//-------------------------------------------------------------
#endif  //__NETWORK_SETUP_H__