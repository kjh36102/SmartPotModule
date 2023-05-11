//RelaySwitchTest.ino

#define PUMP_PIN 18
#define LED_PIN 19

void setup() {
  pinMode(PUMP_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(PUMP_PIN, LOW);
  digitalWrite(LED_PIN, LOW);
}

void loop() {
  digitalWrite(LED_PIN, LOW);
  digitalWrite(PUMP_PIN, LOW);

  delay(3000);

  digitalWrite(PUMP_PIN, HIGH);
  digitalWrite(LED_PIN, HIGH);





  delay(1000);
}
