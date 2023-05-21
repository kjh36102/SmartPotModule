package com.example.app;

import android.content.Context;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpClientRunnable implements Runnable {
    private Context context;

    public UdpClientRunnable(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[1024];

        try {
            DatagramSocket udpSocket = new DatagramSocket();
            byte[] requestData = "request".getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(""), 12345);
            udpSocket.send(requestPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            udpSocket.receive(receivePacket);
            DatagramPacket ackPacket = null;
            String packetData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (packetData.startsWith("SmartPotModule:")) {
                String ipAddress = packetData.substring("SmartPotModule:".length());
                popup.ip = ipAddress;  // IP 주소 저장
                popup.connText.setText("IP정보 수신");
                // ACK 패킷 전송
                String ackMessage = "SmartPotModule:ACK";
                byte[] ackData = ackMessage.getBytes();
                ackPacket = new DatagramPacket(ackData, ackData.length, InetAddress.getByName(popup.ip), 12345);
                udpSocket.send(ackPacket);
            }
            //아두이노가 받으면 broadcast 종료 후 웹서버 실행

            // 3초 대기
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 추가로 패킷 수신 확인
            try {
                udpSocket.setSoTimeout(1000);  // 수신 대기 시간 설정 (1초)
                while (true) {
                    try {
                        udpSocket.receive(receivePacket);
                        // 수신한 패킷 처리
                        packetData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if (packetData.startsWith("SmartPotModule:")) {
                            String ipAddress = packetData.substring("SmartPotModule:".length());
                            popup.ip = ipAddress;  // IP 주소 저장
                            popup.connText.setText("IP정보 수신");
                        }
                        // ACK 패킷 다시 보내기
                        udpSocket.send(ackPacket);
                    } catch (IOException e) {
                        // 수신 대기 시간이 초과되어 예외 발생하면 루프를 종료
                        popup.connText.setText("시간초과, 접속실패");
                        break;
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            // UDP 소켓 닫기
            udpSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
