#include <WiFi.h>
#include <WiFiClient.h>
#include <ArduinoJson.h>

const char *ssid = "LoLin D32 Pro"; // 핫스팟 이름
const char *password = "12345678"; // 핫스팟 비밀번호

WiFiServer server(80); // 80번 포트에서 서버를 시작

void setup() {
  Serial.begin(115200);
  
  WiFi.softAP(ssid, password); // 핫스팟 시작
  
  IPAddress IP = WiFi.softAPIP(); // IP 주소 받아오기
  Serial.print("AP IP address: ");
  Serial.println(IP);

  server.begin(); // 서버 시작
}

void loop() {
  WiFiClient client = server.available(); // 클라이언트 접속 대기

  if (client) {
    String json = ""; // JSON 데이터를 저장할 변수
    bool jsonStart = false; // JSON 시작 태그를 확인하는 플래그

    while (client.connected()) {
      while (client.available()) {
        char c = client.read(); // 클라이언트로부터 데이터 읽기
        json += c;

        if (!jsonStart && json.endsWith("<json>")) {
          jsonStart = true;
          json = ""; // JSON 문자열 초기화
        } else if (jsonStart && json.endsWith("</json>")) {
          json.replace("</json>", ""); // 끝 태그 제거
          
          // JSON 파싱
          DynamicJsonDocument doc(100);
          DeserializationError error = deserializeJson(doc, json);

          if (error) {
            Serial.print("deserializeJson() failed: ");
            Serial.println(error.f_str());
            continue;
          }

          const char* ssid = doc["ssid"];
          const char* pw = doc["pw"];

          // 출력
          Serial.println(ssid);
          Serial.println(pw);
          
          json = ""; // JSON 문자열 초기화
          jsonStart = false; // JSON 시작 플래그 초기화
        }
      }
    }
    client.stop(); // 클라이언트 접속 종료
  }
}
