#include "stdio.h"
#ifndef __ROUTERS_AP_H__
#define __ROUTERS_AP_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "NetworkSetup.h"
#include "UDPLibrary.h"
#include "TimeUpdater.h"
#include "SoilUpdater.h"

//-------------------------------------------------------------
#define LOGKEY "RoutersAP.h"
#include "Logger.h"
//-------------------------------------------------------------

void setupAPRouters() {
  serverAP.on("/regExtWifi", HTTP_GET, []() {
    if (connectPhase != ConnectPhase::SETUP) {
      serverAP.send(200, HTTP_MIME, "err|1|Device is not in setup mode.");
      return;
    }

    if (hasValidArg(serverAP, "ssid") && hasValidArg(serverAP, "pw", false)) {
      STA_SSID = serverAP.arg("ssid");
      STA_PW = serverAP.arg("pw");
      connectPhase = ConnectPhase::STA_INFO;

      if (connectStationAP(STA_SSID, STA_PW, 10000)) {  //타임아웃은 앱보다 좀더 짧게해줘야 연결이 되었는데도 앱이 실패로 판단하는 것을 방지가능
          connectPhase = ConnectPhase::STA_CONNECTED;
          serverSTA.begin();
          serverAP.send(200, HTTP_MIME, "ok|0|Success");
      } else {
        connectPhase = ConnectPhase::INITIAL;
        serverAP.send(200, HTTP_MIME, "err|2|Fail to connect to external network.");
        return;
      }

      if (connectPhase == ConnectPhase::STA_CONNECTED) {
        //UDP Broadcasting 시작
        connectPhase = ConnectPhase::UDP_BROADCAST;

        if (sendUDPMessageUntilACK(("SmartPotModule:" + WiFi.localIP().toString()).c_str(),
                                   "SmartPotModule:ACK", getBroadcastIP(), STA_PORT, 500, 30000)) {
          connectPhase = ConnectPhase::UDP_ACK;
        } else {
          LOGLN(F("UDP ACK응답 받기 시간초과"));
          connectPhase = ConnectPhase::INITIAL;
          return;
        }
      }
    } else {
      serverAP.send(200, HTTP_MIME, "err|0|Query argument is not enough.");
    }
  });
}


//-------------------------------------------------------------
#endif  //__ROUTERS_AP_H__