#include <WiFi.h>

#define LINE2 32
#define LINE1 23

const char* ssid = "Juice";
const char* password = "01086550507";

WiFiServer server(8090);

void setup() {
  pinMode(LINE1, OUTPUT);
  pinMode(LINE2, OUTPUT);
  digitalWrite(LINE1, LOW);
  digitalWrite(LINE2, LOW);

  Serial.begin(115200);
  
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println("WiFi connected");

  server.begin();
  Serial.println("Server started");

}

void loop() {
  WiFiClient client = server.available();
  if (!client) return;

  String request = client.readStringUntil('\r');
  Serial.print("New request: ");
  Serial.println(request);
  client.flush();

  //릴레이 제어
  if (request.indexOf("/code=10") != -1) digitalWrite(LINE1, LOW);
  else if (request.indexOf("/code=11") != -1) digitalWrite(LINE1, HIGH);
  else if (request.indexOf("/code=20") != -1) digitalWrite(LINE2, LOW);
  else if (request.indexOf("/code=21") != -1) digitalWrite(LINE2, HIGH);
  //1번끄기: http://192.168.0.29:8090/code=10
  //2번켜기: http://192.168.0.29:8090/code=21


   // 클라이언트 응답
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html");
  client.println("<!DOCTYPE HTML>");
  client.println("<html>");
  client.println("<head><title>Web Relay Control</title></head>");
  client.println("<body>");
  client.println("<h1>Http Get Relay Control</h1>");
  client.println("<p>LINE1 is now " + String(digitalRead(LINE1)) + "</p>");
  client.println("<p>LINE2 is now " + String(digitalRead(LINE2)) + "</p>");
  client.println("<p><a href=\"/code=11\">Turn LINE1 ON</a></p>");
  client.println("<p><a href=\"/code=10\">Turn LINE1 OFF</a></p>");
  client.println("<p><a href=\"/code=21\">Turn LINE2 ON</a></p>");
  client.println("<p><a href=\"/code=20\">Turn LINE2 OFF</a></p>");
  client.println("</body></html>");

  Serial.println("Response Done.");
}
