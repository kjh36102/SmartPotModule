#include <WiFi.h>
#include <WiFiClient.h>
#include <ArduinoJson.h>
#include "UDPLibrary.h"

const char* ssid = "LoLin D32 Pro";  // 핫스팟 이름
const char* password = "12345678";   // 핫스팟 비밀번호
String remote_ssid = "";
String remote_password = "";
String smartphone_ip = "";
bool connected_same_wifi = false;

WiFiServer server(12345);  // 80번 포트에서 서버를 시작

void setup() {
  Serial.begin(9600);

  WiFi.softAP(ssid, password);  // 핫스팟 시작

  IPAddress IP = WiFi.softAPIP();  // IP 주소 받아오기
  Serial.print("AP IP address: ");
  Serial.println(IP);

  server.begin();  // 서버 시작
}

void loop() {


  if (!connected_same_wifi) {
    WiFiClient client = server.available();  // 클라이언트 접속 대기

    String json = "";        // JSON 데이터를 저장할 변수
    bool jsonStart = false;  // JSON 시작 태그를 확인하는 플래그

    while (client && client.connected()) {
      while (client.available()) {
        char c = client.read();  // 클라이언트로부터 데이터 읽기
        json += c;

        if (!jsonStart && json.endsWith("<json>")) {
          jsonStart = true;
          json = "";  // JSON 문자열 초기화
        } else if (jsonStart && json.endsWith("</json>")) {
          json.replace("</json>", "");  // 끝 태그 제거

          // JSON 파싱
          DynamicJsonDocument doc(100);
          DeserializationError error = deserializeJson(doc, json);

          if (error) {
            Serial.print("deserializeJson() failed: ");
            Serial.println(error.f_str());
            continue;
          }

          remote_ssid = doc["ssid"].as<String>();
          remote_password = doc["pw"].as<String>();

          // 출력
          Serial.println("전달받은 와이파이 SSID: " + String(remote_ssid));
          Serial.println("전달받은 와이파이 Password: " + String(remote_password));

          // 핫스팟 종료
          WiFi.softAPdisconnect(true);

          // 외부 와이파이에 연결
          WiFi.begin(remote_ssid.c_str(), remote_password.c_str());

          // 외부 와이파이에 연결 대기
          while (WiFi.status() != WL_CONNECTED) {
            Serial.println("외부 와이파이 연결중...");
            delay(500);
          }

          //외부 와이파이 주소 출력
          IPAddress ip = WiFi.localIP();
          Serial.print("외부 와이파이에 연결됨: ");
          Serial.printf("%d.%d.%d.%d\n", ip[0], ip[1], ip[2], ip[3]);

          //같은 네트워크 내 broadcast를 통해 스마트폰에게 외부ip주소 전송하기
          if (sendUDPMessageUntilACK(("SmartPotModule:" + ip.toString()).c_str(), "SmartPotModule:ACK", getBroadcastIP(), 12345, 1000)) {
            Serial.println("UDP 전송 성공!");
            connected_same_wifi = true;
            break;
          }
        }
      }

      if (connected_same_wifi) break;
    }
  } else {
    Serial.println("Now Connected Same Wifi: " + String(connected_same_wifi));
  }

  if (connected_same_wifi) {
    WiFiClient client = server.available();  // listen for incoming clients

    if (client) {                     // if you get a client,
      Serial.println("New Client.");  // print a message out the serial port
      String currentLine = "";        // make a String to hold incoming data from the client
      while (client.connected()) {    // loop while the client's connected
        if (client.available()) {     // if there's bytes to read from the client,
          char c = client.read();     // read a byte, then
          Serial.write(c);            // print it out the serial monitor
          if (c == '\n') {            // if the byte is a newline character

            // if the current line is blank, you got two newline characters in a row.
            // that's the end of the client HTTP request, so send a response:
            if (currentLine.length() == 0) {
              // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
              // and a content-type so the client knows what's coming, then a blank line:
              client.println("HTTP/1.1 200 OK");
              client.println("Content-type:text/html");
              client.println();

              // the content of the HTTP response follows the header:
              client.print("SmartPotModule:SUCCESS"); //미리 정해진 ack값 보내주기

              // The HTTP response ends with another blank line:
              client.println();
              // break out of the while loop:
              break;
            } else {  // if you got a newline, then clear currentLine:
              currentLine = "";
            }
          } else if (c != '\r') {  // if you got anything else but a carriage return character,
            currentLine += c;      // add it to the end of the currentLine
          }
        }
      }
      // close the connection:
      client.stop();
      Serial.println("Client Disconnected.");
    }
  }
}
