#ifndef __UDP_LIBRARY_H__
#define __UDP_LIBRARY_H__

//----------------------------------------
#include <WiFiUdp.h>

//--------------------------------
#define LOGKEY "UDPLibrary.h"
#include "Logger.h"
//--------------------------------

/**
* 이 함수는 현재 WiFi 네트워크의 Broadcast 주소를 알아내 반환한다.
* 
* @return 현재 WiFi 네트워크의 Broadcast 주소
*/
IPAddress getBroadcastIP() {
  IPAddress localIP = WiFi.localIP();
  IPAddress subnetMask = WiFi.subnetMask();
  IPAddress broadcastIP;

  for (int i = 0; i < 4; i++)
    broadcastIP[i] = localIP[i] | (~subnetMask[i]);

  return broadcastIP;
}

/**
* 이 함수는 UDP 통신으로 메세지를 보내는 것을 미리 지정된 응답을 받을 때 까지 일정 간격으로 반복한다.
* 
* @param msg 전송할 메세지
* @param expectAck 기대 응답
* @param targetIP 수신자 IP
* @param targetPort 수신 포트번호
* @param interval 전송 간격
* @return 전송 성공시 true
*/
bool sendUDPMessageUntilACK(const char* msg, const char* expectAck, IPAddress targetIP, unsigned int targetPort, unsigned int interval = 1000, unsigned int timeout = 10000) {
  LOGLN(F("Start UDP Broadcasting..."));
  LOGF("\tmsg: %s\n", msg);
  LOGF("\texpectAck: %s\n", expectAck);
  LOGF("\ttargetIP: %s\n", targetIP.toString().c_str());
  LOGF("\ttargetPort: %d\n", targetPort);
  LOGF("\tinterval: %d\n", interval);

  WiFiUDP udp;
  char ackBuffer[50];

  unsigned long transferStartTime = millis();
  udp.begin(targetPort);  //udp 연결 생성

  while (true) {
    //udp 패킷 전송
    udp.beginPacket(targetIP, targetPort);
    udp.write((const uint8_t*)msg, strlen(msg));
    udp.endPacket();

    //interval 만큼 기다림
    delay(interval);

    // //시간초과 확인
    // if(millis() - transferStartTime > timeout){
    //   LOGLN(F("UDP Broadcast reached to timeout!"));
    //   return false;
    // }

    // 응답이 있는지 확인
    int packetSize = udp.parsePacket();

    if (packetSize) {  //응답이 있으면
      int len = udp.read(ackBuffer, sizeof(ackBuffer) - 1);
      if (len > 0) {
        ackBuffer[len] = '\0';
      }

      if (strcmp(ackBuffer, expectAck) == 0) {  //응답이 ack와 같으면 return true
        udp.stop();
        return true;
      }
    }
  }
}

/*
nTime만큼 ACK를 전송하는 함수
패킷손실에 대비해 여러번 전송
*/
bool sendAckNtime(byte nTime, IPAddress targetIP, unsigned int targetPort, unsigned int interval = 200) {
  WiFiUDP udp;
  udp.begin(STA_PORT);

  //udp 패킷 전송
  for (byte i = 0; i < nTime; i++) {
    udp.beginPacket(getBroadcastIP(), STA_PORT);
    const char* msg = "SmartPotModule:ACK";
    udp.write((const uint8_t*)msg, strlen(msg));
    udp.endPacket();
    delay(interval);
  }

  udp.stop();
}


//----------------------------------------
#endif  // __UDP_LIBRARY_H__