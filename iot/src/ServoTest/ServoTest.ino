#include <ESP32Servo.h>

Servo servo;

void setup() {
  Serial.begin(9600);  // 시리얼 통신을 시작합니다.
  servo.attach(13);    // D13 핀에 서보 모터 연결
}

int angle = 0;

void loop() {
  if (Serial.available() > 0) {   // 시리얼 통신 버퍼에 데이터가 있는 경우
    String data = "";             // 데이터를 저장할 문자열 변수
    while (Serial.available()) {  // 버퍼에서 한 바이트씩 읽어옴
      char c = Serial.read();     // 버퍼에서 한 바이트 읽어옴
      data += c;                  // 문자열 변수에 한 바이트씩 추가함
    }
    // 데이터 처리 코드

    Serial.print("Received" + data);
    angle = data.toInt();
  }

  servo.write(angle);
  delay(100);
}
