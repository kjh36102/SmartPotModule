#ifndef __ROUTERS_AP_H__
#define __ROUTERS_AP_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "NetworkSetup.h"
#include "UDPLibrary.h"

//-------------------------------------------------------------
#define LOGKEY "RoutersAP.h"
#include "Logger.h"
//-------------------------------------------------------------

void setupAPRouters() {
  serverAP.on("/regExtWifi", HTTP_GET, []() {
    if (connectPhase != ConnectPhase::SETUP) {
      serverAP.send(500, HTTP_MIME, F("현재 등록모드가 아님"));
      return;
    }

    if (hasValidArg(serverAP, "ssid") && hasValidArg(serverAP, "pw", false)) {
      STA_SSID = serverAP.arg("ssid");
      STA_PW = serverAP.arg("pw");
      connectPhase = ConnectPhase::STA_INFO;

      if (connectStationAP(STA_SSID, STA_PW)) {
        connectPhase = ConnectPhase::STA_CONNECTED;
        serverAP.send(200, HTTP_MIME, F("연결 성공"));
        serverSTA.begin();
        // delay(3000);
        // WiFi.softAPdisconnect(true);
      } else {
        serverAP.send(500, HTTP_MIME, F("연결 실패"));
        connectPhase = ConnectPhase::SETUP;
      }

      if (connectPhase == ConnectPhase::STA_CONNECTED) {
        //UDP Broadcasting 시작
        connectPhase = ConnectPhase::UDP_BROADCAST;

        if (sendUDPMessageUntilACK(("SmartPotModule:" + WiFi.localIP().toString()).c_str(),
                                   "SmartPotModule:ACK", getBroadcastIP(), STA_PORT, 1000, 30000)) {
          // sendAckNtime(5, getBroadcastIP(), STA_PORT);
          connectPhase = ConnectPhase::UDP_ACK;
        }
      }
    } else {
      serverAP.send(500, "text/plain", "Query argument is not enough.");
    }
  });
}


//-------------------------------------------------------------
#endif  //__ROUTERS_AP_H__