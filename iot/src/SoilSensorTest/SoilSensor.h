#include <SoftwareSerial.h> //<- EspSoftwareSerial.h
#include <Wire.h>

class SoilSensor {
private:
  const byte CODE[8] = { 0x01, 0x03, 0x00, 0x00, 0x00, 0x07, 0x04, 0x08 };
  byte buffer[19] = {
    0,
  };
  float receive[7] = {
    0.0,
  };
  EspSoftwareSerial::UART soilSerial;
  uint8_t pin_DE_RE;
  uint8_t pin_RO;
  uint8_t pin_DI;

public:
  SoilSensor(uint8_t DE_RE, uint8_t RO, uint8_t DI) {
    this->pin_DE_RE = DE_RE;
    this->pin_RO = RO;
    this->pin_DI = DI;

    this->soilSerial.begin(4800, SWSERIAL_8N1, this->pin_RO, this->pin_DI, false);

    pinMode(this->pin_DE_RE, OUTPUT);
    digitalWrite(this->pin_DE_RE, LOW);
  }

  float* read() {
    digitalWrite(this->pin_DE_RE, HIGH);
    this->soilSerial.write(this->CODE, 8);
    digitalWrite(this->pin_DE_RE, LOW);

    delay(200);

    for (byte i = 0; i < 19; i++) {
      this->buffer[i] = this->soilSerial.read();
    }

    for (byte i = 0; i < 7; i++) {
      this->receive[i] = this->concat_byte(buffer, 3 + 2 * i);
    }

    //일부 값 10으로 나눠서 전처리
    this->receive[0] /= 10;
    this->receive[1] /= 10;
    this->receive[3] /= 10;

    return this->receive;
  }

  uint16_t concat_byte(uint8_t* buff, uint8_t startIdx) {
    return (((uint16_t)(buff[startIdx] << 8) | (buff[startIdx + 1])));
  }
};