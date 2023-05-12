#include <WiFi.h>
#include <WiFiClient.h>
#include <ArduinoJson.h>

const char* ssid = "LoLin D32 Pro";  // 핫스팟 이름
const char* password = "12345678";   // 핫스팟 비밀번호

WiFiServer server(80);  // 80번 포트에서 서버를 시작

void setup() {
  Serial.begin(115200);

  WiFi.softAP(ssid, password);  // 핫스팟 시작

  IPAddress IP = WiFi.softAPIP();  // IP 주소 받아오기
  Serial.print("AP IP address: ");
  Serial.println(IP);

  server.begin();  // 서버 시작
}

void loop() {
  WiFiClient client = server.available();  // 클라이언트 접속 대기

  if (client) {
    String json = "";        // JSON 데이터를 저장할 변수
    bool jsonStart = false;  // JSON 시작 태그를 확인하는 플래그

    while (client.connected()) {
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

          const char* remote_ssid = doc["ssid"];
          const char* remote_pw = doc["pw"];

          // 출력
          Serial.println(remote_ssid);
          Serial.println(remote_pw);

          json = "";          // JSON 문자열 초기화
          jsonStart = false;  // JSON 시작 플래그 초기화

          // Disconnect from the AP
          WiFi.softAPdisconnect(true);

          // Connect to the external network
          WiFi.begin(remote_ssid, remote_pw);

          // Wait for connection
          while (WiFi.status() != WL_CONNECTED) {
            delay(500);
            Serial.println("Connecting to WiFi..");
          }

          // Print the IP address
          Serial.println(WiFi.localIP());

          // Send the IP address back to the client
          client.println(WiFi.localIP().toString());

          // Disconnect from the external network
          // WiFi.disconnect();

          // Restart the access point
          // WiFi.softAP(ssid, password);

          //스마트폰은 아두이노의 핫스팟에 계속 연결을 시도하고 있어야함
          
          //스마트폰으로 esp32의 외부 네트워크 ip주소 전송

          //핫스팟을 끈다

          //스마트폰은 전달받은 esp32의 ip주소를 가지고 있는 채 같은 외부네트워크에 연결한다.

          //스마트폰이 esp32와 동일네트워크에 연결되면 esp32의 ip주소를 타겟으로 데이터(스마트폰의 ip주소)를 전송한다.

          //상호 ip주소 정보를 저장하고 통신한다.
        }
      }
    }
    client.stop();  // 클라이언트 접속 종료
  }
}