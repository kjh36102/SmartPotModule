#include <WiFi.h>
#include "time.h"
#include "esp_sleep.h"

const char* ssid = "Juice";
const char* password = "01086550507";
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 3600 * 9;  // Adjust based on your timezone
const int daylightOffset_sec = 0;     // Adjust based on your daylight saving

gpio_num_t GPIO_POWER = (gpio_num_t)14;

void printLocalTime() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return;
  }
  Serial.println(&timeinfo, "%A, %B %d %Y %H:%M:%S");
}

void connectToWiFiAndNTP() {
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }

  Serial.println("Connected to WiFi");

  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  printLocalTime();
}

void setup() {
  Serial.begin(9600);

  pinMode(GPIO_POWER, INPUT);
  esp_sleep_enable_ext0_wakeup(GPIO_POWER, LOW);

  if (esp_sleep_get_wakeup_cause() == ESP_SLEEP_WAKEUP_EXT0) {
    Serial.println("Awake from deepsleep!");
  }
}

void loop() {

  int powerState = digitalRead(GPIO_POWER);

  if (powerState == LOW) {
    Serial.println("Going into deepsleep...");
    esp_deep_sleep_start();
  } else {
    String command;

    if (Serial.available()) {
      command = Serial.readString();

      if (command.startsWith("update")) {
        connectToWiFiAndNTP();
      }
    }

    printLocalTime();
  }

  delay(1000);
}
