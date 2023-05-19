//Main.ino

//라이브러리들 include
#include "PinSetup.h"
#include "MultitaskRTOS.h"

//태스크 파일들 include
#include "TaskConnWithApp.h"

//--------------------------------
#define LOGKEY "Main.ino"
#include "Logger.h"
//--------------------------------

void setup() {
  Serial.begin(9600);

  //로거 켜고끄기, 주석으로 끔
  enableLogging("Main.ino");
  enableLogging("PinSetup.h");
  enableLogging("MultitaskRTOS.h");
  enableLogging("TaskConnWithApp.h");
  enableLogging("UDPLibrary.h");
  // enableLogging("HttpGetLibrary.h");

  initPins();  //핀 초기화

  createAndRunTask(tReceiveExtWifiInfo, "TaskConnWithApp", 3000);
}

void loop() {
  LOGF("External SSID: %s, External PW: %s\n", externalSSID.c_str(), externalPassword.c_str());
  LOGF("External IP: %s, Smartphone IP: %s\n", externalIP.toString().c_str(), smartphoneIP.toString().c_str());
  LOGF("stateReceivedWifiInfo: %d\tstateConnExtWifi: %d\tstateConnSameWifi: %d\n", stateReceivedWifiInfo, stateConnExtWifi, stateConnSameWifi);
  delay(5000);
}
