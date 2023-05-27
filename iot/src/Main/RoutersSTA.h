// #include "WiFiType.h"
#ifndef __ROUTERS_STA_H__
#define __ROUTERS_STA_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "NetworkSetup.h"

//-------------------------------------------------------------
#define LOGKEY "RoutersSTA.h"
#include "Logger.h"
//-------------------------------------------------------------

void setupSTARouters() {
  serverSTA.on("/hello", HTTP_GET, []() {
    if (WiFi.status() != WL_CONNECTED) {
      serverSTA.send(500, HTTP_MIME, F("외부 네트워크에 아직 연결되지 않음"));
      return;
    }

    smartphoneIP = serverSTA.client().remoteIP();
    connectPhase = ConnectPhase::COMPLETE;

    WiFi.softAPdisconnect(true);  //핫스팟끄기

    serverSTA.send(200, HTTP_MIME, "All connected!");

    LOGLN(F("성공적으로 앱과 연결됨!"));
    LOGF("\t스마트폰IP: %s\n\t외부네트워크IP: %s\n", smartphoneIP.toString().c_str(), WiFi.localIP().toString().c_str());
  });

  serverSTA.on("/testData", HTTP_GET, [](){
    serverSTA.send(200, HTTP_MIME, "테스트 데이터 받기~");
  });
}


//-------------------------------------------------------------
#endif  //__ROUTERS_STA_H__