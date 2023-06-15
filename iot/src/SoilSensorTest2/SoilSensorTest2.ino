#include <ModbusMaster.h>
#include <SoftwareSerial.h>

// #define MAX485_DE      2
// #define MAX485_RE_NEG  4

#define DE_RE 18

EspSoftwareSerial::UART RS485Serial; // RX, TX
ModbusMaster node;

void preTransmission() {
  // digitalWrite(MAX485_RE_NEG, 1);
  // digitalWrite(MAX485_DE, 1);
  digitalWrite(DE_RE, 1);
}

void postTransmission() {
  // digitalWrite(MAX485_RE_NEG, 0);
  // digitalWrite(MAX485_DE, 0);
  digitalWrite(DE_RE, 0);
}

void setup() {
  Serial.begin(9600);
  // pinMode(MAX485_RE_NEG, OUTPUT);
  // pinMode(MAX485_DE, OUTPUT);
  pinMode(DE_RE, OUTPUT);

  postTransmission();

  RS485Serial.begin(4800, SWSERIAL_8N1, 19, 21, false);

  node.begin(1, RS485Serial);
  node.preTransmission(preTransmission);
  node.postTransmission(postTransmission);
}

void loop() {
  uint8_t result;
  uint16_t data[7];

  result = node.readHoldingRegisters(0, 7);
  
  if (result == node.ku8MBSuccess) {
    for (int i = 0; i < 7; i++) {
      data[i] = node.getResponseBuffer(i);
    }
    Serial.println("Humidity: " + String(data[0]));
    Serial.println("Temprature: " + String(data[1]));
    Serial.println("EC: " + String(data[2]));
    Serial.println("PH: " + String(data[3]));
    Serial.println("Nitro: " + String(data[4]));
    Serial.println("Phos: " + String(data[5]));
    Serial.println("Pota: " + String(data[6]));
    Serial.println();
  }
  else {
    Serial.println("Read failed.");
    Serial.println();
  }
  delay(1000);
}
