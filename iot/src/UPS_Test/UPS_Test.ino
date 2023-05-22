#define EXTPOWER_READ 12
#define CUT_BATTERY 13
#define INTERNAL_LED 5


void setup() {
  // put your setup code here, to run once:
  pinMode(INTERNAL_LED, OUTPUT);
  pinMode(CUT_BATTERY, OUTPUT);
  pinMode(EXTPOWER_READ, INPUT);

  digitalWrite(INTERNAL_LED, 0);
  digitalWrite(CUT_BATTERY, 0);
  delay(3000);
}


void blinkNtime(int n) {
  for (byte i = 0; i < n; i++) {
    digitalWrite(INTERNAL_LED, HIGH);
    delay(200);
    digitalWrite(INTERNAL_LED, LOW);
    delay(200);
  }
  delay(1000);
}

void blinkOnShutdown(){
  for (byte i = 0; i < 30; i++) {
    digitalWrite(INTERNAL_LED, HIGH);
    delay(50);
    digitalWrite(INTERNAL_LED, LOW);
    delay(50);
  }
}

int i = 0;
void loop() {

  while(!digitalRead(EXTPOWER_READ)) {
    digitalWrite(CUT_BATTERY, 0);


    blinkNtime(i + 1);

    i += 1;
    if (i >= 3) i = 0;
  }

  blinkOnShutdown();
  digitalWrite(CUT_BATTERY , 1);
}
