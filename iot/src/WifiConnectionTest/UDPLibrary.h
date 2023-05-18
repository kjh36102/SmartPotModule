#include <WiFiUdp.h>

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
bool sendUDPMessageUntilACK(const char* msg, const char* expectAck, IPAddress targetIP, unsigned int targetPort, unsigned int interval) {
  Serial.println("SendUDPMessageUntilACK Running...");
  Serial.printf("\tmsg: %s\n", msg);
  Serial.printf("\texpectAck: %s\n", expectAck);
  Serial.printf("\ttargetIP: %s\n", targetIP.toString().c_str());
  Serial.printf("\ttargetPort: %d\n", targetPort);
  Serial.printf("\tinterval: %d\n", interval);


  WiFiUDP udp;
  char ackBuffer[50] = {'\0', };

  udp.begin(0); //udp 연결 생성

  while (true) {
    //udp 패킷 전송
    udp.beginPacket(targetIP, targetPort);
    udp.write((const uint8_t*)msg, strlen(msg));
    udp.endPacket();

    //interval 만큼 기다림
    delay(interval);

    // 응답이 있는지 확인
    int packetSize = udp.parsePacket();
    if (packetSize) {
      // received a packet
      Serial.printf("Received %d bytes from %s, port %d\n", packetSize, udp.remoteIP().toString().c_str(), udp.remotePort());
      int len = udp.read(ackBuffer, sizeof(ackBuffer) - 1);
      if (len > 0) {
        ackBuffer[len] = '\0';
      }
      Serial.printf("UDP packet contents: %s\n", ackBuffer);

      if (strcmp(ackBuffer, expectAck) == 0) {
        udp.stop();
        return true;
      }
    }
  }
}