//RelaySwitchTest.ino

#define PUMP_PIN 12
#define LED_PIN 13

void setup() {
  pinMode(PUMP_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(PUMP_PIN, LOW);
  digitalWrite(LED_PIN, LOW);
}

void loop() {
  digitalWrite(PUMP_PIN, HIGH);
  delay(5000);
  digitalWrite(PUMP_PIN, LOW);
  // delay(5000);
  digitalWrite(LED_PIN, HIGH);
  delay(5000);
  digitalWrite(LED_PIN, LOW);

}
