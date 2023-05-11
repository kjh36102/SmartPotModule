// // 팬 속도 조절
// const int LED_PIN = 5;
// const int LEDC_CHANNEL = 0;
// const int LEDC_FREQUENCY = 10;
// const int LEDC_RESOLUTION = 8;


// 조명 밝기 조절
const int LED_PIN = 5;
const int LEDC_CHANNEL = 0;
const int LEDC_FREQUENCY = 5000;
const int LEDC_RESOLUTION = 8;

void setup() {
  ledcSetup(LEDC_CHANNEL, LEDC_FREQUENCY, LEDC_RESOLUTION);
  ledcAttachPin(LED_PIN, LEDC_CHANNEL);
  Serial.begin(9600);  // 시리얼 통신을 시작합니다.
}

int pwm = 0;

void loop() {
  if (Serial.available() > 0) {   // 시리얼 통신 버퍼에 데이터가 있는 경우
    String data = "";             // 데이터를 저장할 문자열 변수
    while (Serial.available()) {  // 버퍼에서 한 바이트씩 읽어옴
      char c = Serial.read();     // 버퍼에서 한 바이트 읽어옴
      data += c;                  // 문자열 변수에 한 바이트씩 추가함
    }
    // 데이터 처리 코드

    Serial.print("Received" + data);
    pwm = data.toInt();
  }

  ledcWrite(LEDC_CHANNEL, pwm);
  delay(100);
}