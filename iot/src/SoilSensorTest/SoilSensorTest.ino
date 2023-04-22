//https://drive.google.com/file/d/1o4dCYjejzNtKOGQ-wII0Ji5hKu9edk3i/view

#include <SoftwareSerial.h>
#include <Wire.h>

#define DE_RE 18
#define RO 14
#define DI 27

const byte code_nitro[8] = { 0x01, 0x03, 0x00, 0x1e, 0x00, 0x01, 0xE4, 0x0C };
const byte code_phos[8] = { 0x01, 0x03, 0x00, 0x1f, 0x00, 0x01, 0xb5, 0xCC };
const byte code_pota[8] = { 0x01, 0x03, 0x00, 0x20, 0x00, 0x01, 0x85, 0xC0 };
const byte code_moist[8] = { 0x01, 0x03, 0x00, 0x12, 0x00, 0x01, 0x24, 0x0F };
const byte code_ec[8] = { 0x01, 0x03, 0x00, 0x15, 0x00, 0x01, 0x95, 0xCE };

SoftwareSerial soilSerial(RO, DI);

unsigned short measure(const byte* code, byte (*receive_buffer)[7]){
  digitalWrite(DE_RE, 1);
  delay(10);

  if (soilSerial.write(code, 8) == 8) {
    digitalWrite(DE_RE, 0);
    delay(200);

    for (byte i = 0; i < 7; i++) {
      (*receive_buffer)[i] = soilSerial.read();
    }

    return (((unsigned short)(*receive_buffer)[3] << 8) | (*receive_buffer)[4]);
  }

  Serial.print("Failed to read code: ");
  for(byte i = 0; i < 8; i++){
    Serial.print(code[i], HEX);
    Serial.print(" ");
  }
  Serial.println();
}

unsigned short printValue(const char* name, const byte* code){
  Serial.println(name);
  Serial.print("\tquery:\t");

  for(byte i = 0; i < 8; i++){
    Serial.print(code[i], HEX);
    Serial.print(" ");
  }
  Serial.println();

  byte receive_buffer[7] = {0, };
  unsigned short value = measure(code, &receive_buffer);

  Serial.print("\tvalue:\t");
  for(byte i = 0; i < 7; i++){
    Serial.print(receive_buffer[i]);
    Serial.print(" ");
  }
  Serial.print("\t");
  Serial.println(value);

  return value;
}

void setup() {
  Serial.begin(9600);
  soilSerial.begin(4800);

  pinMode(DE_RE, OUTPUT);

  delay(500);
}

void loop() {
  //   // Print values to the serial monitor
  Serial.println("------------------------------------");
  printValue("Nitrogen", code_nitro);
  printValue("Phosphorous", code_phos);
  printValue("Potassium", code_pota);
  printValue("Moisture", code_moist);
  printValue("EC", code_ec);
  delay(2000);
}
