#include <WiFi.h>
#include <WebServer.h>

const char* ssid = "Barrier";
const char* password = "01063445377";

WebServer server(12345);

String globalVar = "initial value";

void setup() {
  Serial.begin(9600);
  
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  
  Serial.println("Connected to WiFi");

  server.on("/", HTTP_GET, []() {
    if (server.hasArg("newVal")) {
      globalVar = server.arg("newVal");
    }
    server.send(200, "text/plain", "Global variable is now: " + globalVar);
  });

  server.on("/", HTTP_POST, []() {
    if (server.hasArg("plain")) {
      globalVar = server.arg("plain");
    }
    server.send(200, "text/plain", "Global variable is now: " + globalVar);
  });

  server.begin();

   xTaskCreatePinnedToCore(
    task1,       /* Function to implement the task */
    "task1",     /* Name of the task */
    10000,       /* Stack size in words */
    NULL,        /* Task input parameter */
    0,           /* Priority of the task */
    NULL,        /* Task handle. */
    0);          /* Core where the task should run */

  xTaskCreatePinnedToCore(
    task2,       
    "task2",     
    10000,       
    NULL,        
    0,           
    NULL,        
    1);          /* Run this task on core 1 */
}

void task1(void * parameter)
{
  while(1) {
    Serial.println("Task 1 running on core 0");
    delay(1000);
  }
}

void task2(void * parameter)
{
  while(1) {
    Serial.println("Task 2 running on core 1");
    delay(1000);
  }
}

void loop() {
  server.handleClient();
}
