#ifndef __TASK_CONN_WITH_APP__
#define __TASK_CONN_WITH_APP__

//----------------------------------------
#include <WiFi.h>
#include <WiFiClient.h>
#include "UDPLibrary.h"
#include "HttpGetLibrary.h"
#include "MultitaskRTOS.h"

#define PROTOCOL_PORT 12345

//--------------------------------
#define LOGKEY "TaskConnWithApp.h"
#include "Logger.h"
#define PRINT_STACK_USAGE true
//--------------------------------

const char* HOTSPOT_SSID = "SmartPotModule";
// const char* HOSTSPOT_PASSWORD = "";

String externalSSID = "";
String externalPassword = "";
IPAddress externalIP = IPAddress(0, 0, 0, 0);
IPAddress smartphoneIP = IPAddress(0, 0, 0, 0);
bool stateConnSameWifi = false;
bool stateReceivedWifiInfo = false;
bool stateConnExtWifi = false;

WiFiServer httpServer(PROTOCOL_PORT);

void initTaskConnWithApp();
void tReceiveExtWifiInfo(void* taskParams);
void tConnExtWifiAndBroadcastUDP(void* taskParams);

void initTaskConnWithApp() {
  //변수 초기화
  externalSSID = "";
  externalPassword = "";
  externalIP = IPAddress(0, 0, 0, 0);
  smartphoneIP = IPAddress(0, 0, 0, 0);
  stateConnSameWifi = false;
  stateReceivedWifiInfo = false;
  stateConnExtWifi = false;

  WiFi.softAP(HOTSPOT_SSID);  // 핫스팟 시작

  LOGF("\tHotspot IP Address: %s\n", WiFi.softAPIP().toString().c_str());

  httpServer.begin();  // http서버 시작
}

void tReceiveExtWifiInfo(void* taskParams) {
  initTaskConnWithApp();  //시작시 1회 실행

  for (;;) {

    WiFiClient client = httpServer.available();
    if (client) {
      if (client.available()) {
        std::map<String, String> queryMap = getQueryMap(client, 100);  //최대 100바이트 읽기

        //쿼리 파싱
        if (!queryMap.empty()) {
          String action = queryMap["action"];

          if (action.equals("regExtWifi")) {
            externalSSID = queryMap["ssid"];
            externalPassword = queryMap["pw"];
            LOGF("\tReceived externalSSID: %s\texternalPassword: %s\n", externalSSID.c_str(), externalPassword.c_str());
            sendHttpResponse(client, 200, "OK");
            LOGLN("Received External Wifi Info Successfully!");
          } else {  //알수 없는 action인경우
            sendHttpResponse(client, 500, "Unknown Action");
            LOGF("Client sent unknown action: %s\n", action.c_str());
            continue;
          }

          client.stop();

          stateReceivedWifiInfo = true;

          //핫스팟 종료
          WiFi.softAPdisconnect(true);

          unsigned long connectStartTime = millis();

          // 외부 와이파이에 연결
          WiFi.begin(externalSSID.c_str(), externalPassword.c_str());

          //외부 와이파이 상태 확인
          while (WiFi.status() != WL_CONNECTED) {
            if (millis() - connectStartTime > 10000) {  // 10초가 지나면 실패
              LOGLN("Connection time out!!");
              break;
            }
            LOGLN("Connecting to external wifi...");
            delay(500);
          }

          if (WiFi.status() == WL_CONNECTED) {  //성공적 연결
            stateConnExtWifi = true;
            LOGLN("Connect to external wifi success!");
          } else {  //정보오류로 실패하면
            LOGLN("Connect to external wifi failed! Return to turn hotspot on.");
            initTaskConnWithApp();  //초기화 및 재실행
            continue;
          }
          LOGF("\tConnected externalSSID: %s\texternalPassword: %s\n", externalSSID.c_str(), externalPassword.c_str());

          if (stateConnExtWifi) {  //외부 네트워크 연결 성공하면
            //외부 와이파이 주소 저장
            externalIP = WiFi.localIP();
            LOGF("\texternalIP: %s\n", externalIP.toString().c_str());

            //같은 네트워크 내 broadcast를 통해 스마트폰에게 외부ip주소 전송하기
            if (sendUDPMessageUntilACK(("SmartPotModule:" + externalIP.toString()).c_str(), "SmartPotModule:ACK", getBroadcastIP(), PROTOCOL_PORT, 2000)) {
              LOGLN("Inform externalIP to App Success!");
            }

            createAndRunTask(tConnExtWifiAndBroadcastUDP, "ConnExtWifiAndBroadcastUDP", 3000);
            deleteTask(NULL);  //웹서버 실행하는 태스크 실행하고 현재태스크는 종료
          }
        } else {  //client가 빈 query를 보내거나 favicon.ico요청이 들어오는경우
          sendHttpResponse(client, 500, "Received Empty Query");
          LOGLN("Received empty query, send status 500");
        }
      }
    }

    // Watchdog 재설정
    vTaskDelay(1);  // 매 반복마다 Watchdog를 재설정
  }
}

void tConnExtWifiAndBroadcastUDP(void* taskParams) {
  for (;;) {

    if (stateReceivedWifiInfo & stateConnExtWifi) {
      WiFiClient client = httpServer.available();

      if (client) {
        std::map<String, String> queryMap = getQueryMap(client, 100);  //최대 40바이트 읽기

        //쿼리 파싱
        if (!queryMap.empty()) {
          String action = queryMap["action"];

          if (action.equals("hello")) {  // http://x.x.x.x:12345/?action=hello
            smartphoneIP = client.remoteIP();
            LOGF("\tReceived action: %s\tclientIP: %s\n", action.c_str(), smartphoneIP.toString().c_str());
            sendHttpResponse(client, 200, "Now All Connected");
            stateConnSameWifi = true;
          } else {  //알수 없는 action인경우
            sendHttpResponse(client, 500, "Unknown Action");
            LOGF("Client sent unknown action: %s\n", action.c_str());
          }

        } else {  //client가 빈 query를 보내거나 favicon.ico요청이 들어오는경우
          sendHttpResponse(client, 500, "Received Empty Query");
          LOGLN("Received empty query, send status 500");
        }

        client.stop();

        //유효성 검증
        if (stateReceivedWifiInfo & stateConnExtWifi & stateConnSameWifi) {
          LOGLN("Successfully connected with App!");
          deleteTask(NULL);
        } else {
          LOGLN("Err: There is false state somewhere.");
        }
      }

      vTaskDelay(1);
    }
  }
}
#endif  // __TASK_CONN_WITH_APP__