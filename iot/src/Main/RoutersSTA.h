// #include "WiFiType.h"
#ifndef __ROUTERS_STA_H__
#define __ROUTERS_STA_H__

//-------------------------------------------------------------
#include <Arduino.h>
#include "NetworkSetup.h"
#include "ArduinoJson.h"
#include "SoilSensor.h"

//-------------------------------------------------------------
#define LOGKEY "RoutersSTA.h"
#include "Logger.h"
//-------------------------------------------------------------




void addToJsonDoc(const String& key, StaticJsonDocument<200>& doc);

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

  serverSTA.on("/testData", HTTP_GET, []() {
    serverSTA.send(200, HTTP_MIME, "테스트 데이터 받기~");
  });

  /**
  토양센서 값을 읽어서 쿼리가 있는값만 json으로 응답
  */
  // serverSTA.on(
  //   "/getSoilData", HTTP_GET, []() {
  //     if (!hasValidArg(serverSTA, "col")) {
  //       serverSTA.send(400, "application/json", F("{\"error\":\"missing col argument\"}"));
  //       return;
  //     }

  //     String colArg = serverSTA.arg("col");
  //     StaticJsonDocument<200> doc;

  //     int fromIndex = 0;
  //     int toIndex = colArg.indexOf('_');
  //     while (toIndex != -1) {
  //       String key = colArg.substring(fromIndex, toIndex);
  //       addToJsonDoc(key, doc);
  //       fromIndex = toIndex + 1;
  //       toIndex = colArg.indexOf('_', fromIndex);
  //     }

  //     // 마지막 키 추가
  //     addToJsonDoc(colArg.substring(fromIndex), doc);

  //     String output;
  //     serializeJson(doc, output);

  //     serverSTA.send(200, "application/json", output);
  //   });
}

// void addToJsonDoc(const String& key, StaticJsonDocument<200>& doc) {
//   float* received = soilSensor.read();

//   if (key == "humid")
//     doc["humid"] = String(received[0], 1);
//   else if (key == "temp")
//     doc["temp"] = String(received[1], 1);
//   else if (key == "ec")
//     doc["ec"] = received[2];
//   else if (key == "ph")
//     doc["ph"] = String(received[3], 1);
//   else if (key == "nitro")
//     doc["nitro"] = received[4];
//   else if (key == "phos")
//     doc["phos"] = received[5];
//   else if (key == "pota")
//     doc["pota"] = received[6];
// }


//-------------------------------------------------------------
#endif  //__ROUTERS_STA_H__