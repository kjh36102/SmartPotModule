#include "WiFiType.h"
// #include "IPAddress.h"
#ifndef __NETWORK_SETUP_H__
#define __NETWORK_SETUP_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include "UDPLibrary.h"
#include "NextOperationHandler.h"
#include "SoilUpdater.h"
#include "DB_Manager.h"

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
  // REFRESH,
  RECONNECT
};

ConnectPhase connectPhase = ConnectPhase::INITIAL;

const char* HTTP_MIME = "text/plain; charset=utf-8";
String previousTryingSSID;
String previousTryingPW;
bool connectStationAP(String SSID, String PW, unsigned long timeout = 30000) {

  if (SSID.equals(previousTryingSSID) && PW.equals(previousTryingPW)) return false;

  previousTryingSSID = SSID;
  previousTryingPW = PW;

  //STA 연결 시도
  unsigned long staConnStartTime = millis();
  WiFi.begin(SSID.c_str(), PW.c_str());

  bool ret = true;
  LOG(("외부 네트워크 연결중.."));
  while (WiFi.status() != WL_CONNECTED) {

    if (millis() - staConnStartTime >= timeout) {
      LOGLN(("\n\t연결시간 초과!"));
      ret = false;
      break;
    }
    LOG(".");
    delay(500);
  }

  if (ret) {
    previousTryingSSID = "";
    previousTryingPW = "";

    LOGLN(("\n\t연결됨!"));
    LOGF("\tSSID: %s\n\tPW: %s\n\t외부IP: %s\n", SSID.c_str(), PW.c_str(), WiFi.localIP().toString().c_str());
  }

  return ret;
}

void initNetwork(bool clearAll = false) {

  connectPhase = ConnectPhase::SETUP;
  NextOperationHandler::stopTasks();
  SoilUpdater::getInstance().off();

  if (clearAll) {
    STA_SSID = "";
    STA_PW = "";
    smartphoneIP = IPAddress(0, 0, 0, 0);
  } else {
    //db로부터 데이터 로드하기
    DB_Manager& dbManager = DB_Manager::getInstance();

    dbManager.execute("select * from wifi_info");
    JsonObject row = dbManager.getRowFromJsonArray(dbManager.getResult(), 0);

    const char* ssid_sta = row["ssid_sta"];
    const char* pw_sta = row["pw_sta"];
    const char* phone_ip = row["phone_ip"];

    //기존 정보가 존재하는지 확인
    if (strlen(ssid_sta) > 0 && strlen(phone_ip) > 0) {  //비밀번호는 비어있을수 있으니 체크 안함
      LOGF("기존 와이파이정보 발견! SSID: %s, PW: %s, Phone IP: %s\n", ssid_sta, pw_sta, phone_ip);

      STA_SSID = String(ssid_sta);
      STA_PW = String(pw_sta);
      smartphoneIP.fromString(String(phone_ip));

      connectPhase = ConnectPhase::RECONNECT;
    } else {
      LOGLN(("저장된 와이파이 정보가 없음"));
    }
  }


  //와이파이와 서버  끄기
  serverAP.stop();
  serverSTA.stop();
  WiFi.softAPdisconnect(true);
  WiFi.disconnect();

  LOG(("와이파이 연결 해제중.."));
  while (WiFi.status() == WL_CONNECTED && WiFi.status() != WL_DISCONNECTED) {  //연결해제 기다리기
    LOG(".");
    delay(100);
  }
  LOGLN(("\n\t연결 해제됨!"));

  bool staConnFailed = false;
  if (STA_SSID.length() != 0) {  //외부네트워크 정보 있음
    if (connectStationAP(STA_SSID, STA_PW)) {
      serverSTA.begin();
      delay(200);
      NextOperationHandler::startTasks();
      SoilUpdater::getInstance().on();
      connectPhase = ConnectPhase::COMPLETE;
      return;
    } else {
      LOGLN(("기존 정보로 연결에 실패함, 초기화"));
      STA_SSID = "";
      STA_PW = "";
    }
  }

  delay(200);

  if (connectPhase == ConnectPhase::SETUP) {
    //AP 실행
    if (strcmp(AP_PW, "") == 0)
      WiFi.softAP(AP_SSID);
    else WiFi.softAP(AP_SSID, AP_PW);
    LOGLN(("핫스팟 실행됨"));

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