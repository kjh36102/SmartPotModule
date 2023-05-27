package xyz.cofon.wificonnectapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    public static boolean CONNECT_STATE = false;    //현재 아두이노와의 연결상태
    public static String EXTERNAL_IP;   //아두이노의 외부네트워크 ip
    public static String EXTERNAL_SSID; //이전 연결에 사용한 외부네트워크의 ssid
    public static String EXTERNAL_PW;   //이전 연결에 사용한 외부네트워크의 pw

    private EditText ssidEditText;
    private EditText passwordEditText;
    private Button connectButton;
    private Button requestDataButton;
    private TextView statusLabel;

    WifiConnectionManager connManager;

    public void startConnect() {

        CONNECT_STATE = false;
        EXTERNAL_IP = null;

        int hotspotTimeout = 30000; //연결을 수락하고 최종적으로 연결될때까지의 시간, 따라서 넉넉히 줘야함
        connManager.connectToHotspot("SmartPotModule", hotspotTimeout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssidEditText = findViewById(R.id.editTextSsid);
        passwordEditText = findViewById(R.id.editTextPassword);
        connectButton = findViewById(R.id.connectButton);
        requestDataButton = findViewById(R.id.requestDataButton);
        statusLabel = findViewById(R.id.statusLabel);

        /*
         * xyz.cofon.wificonnectapp system.
         * 로그캣 System out 필터
         * */

        //WifiConnectManager 객체 생성 후 activity 인스턴스와 상태표시 Textview 넘겨주기
        connManager = new WifiConnectionManager(this, this.statusLabel);

        //필요한 권한 있는지 확인하고 하나라도 없으면 모두 요청
        if (!connManager.permission.hasAll())
            connManager.permission.requestAll();

        //처음 안내
        connManager.setStatusText("SSID 및 비밀번호를 입력하세요");
        System.out.println("SSID 및 비밀번호를 입력하세요");


        //연결 버튼 리스너 등록
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnect();
            }
        });

        //핫스팟이 연결되면 실행될 콜백 정의
        connManager.setOnHotspotAvailable(() -> {
            connManager.setStatusText("핫스팟 이용가능, 아두이노에게 전송 시작함...");
            System.out.println("핫스팟 이용가능, 아두이노에게 전송 시작함...");

            String externalSSID = ssidEditText.getText().toString().trim();
            String externalPW = passwordEditText.getText().toString().trim();

            //연결 성공되었으므로 외부네트워크 정보를 아두이노에 전송함
            connManager.sendExternalWifiInfo(
                    externalSSID,
                    externalPW,
                    30000,  //타임아웃
                    () -> {    // 성공시 콜백
                        connManager.setStatusText("아두이노가 외부네트워크에 연결됨");
                        System.out.println("아두이노가 외부네트워크에 연결됨");

                        //외부네트워크로 변경해주기
                        int externalTimeout = 30000;    //연결을 수락하고 최종적으로 연결될때까지의 시간, 따라서 넉넉히 줘야함
                        connManager.connectToExternal(externalSSID, externalPW, externalTimeout);
                    },
                    () -> {    // 실패시 콜백
                        connManager.setStatusText("아두이노가 외부네트워크에 연결을 실패함");
                        System.out.println("아두이노가 외부네트워크에 연결을 실패함");
                    },
                    () -> {    // 에러시 콜백
                        Exception e = connManager.getCurrentException();  //오류 가져오기

                        if (e instanceof SocketException) {        //오류 종류에따른 처리
                            connManager.setStatusText("아두이노에게 정보전송 중 연결 실패오류");
                            System.out.println("아두이노에게 정보전송 중 연결 실패오류");
                        } else if (e instanceof SocketTimeoutException) {
                            connManager.setStatusText("아두이노에게 정보전송 타임아웃");
                            System.out.println("아두이노에게 정보전송 타임아웃");
                        } else {
                            connManager.setStatusText("아두이노에게 정보전송중 오류발생");
                            System.out.println("아두이노에게 정보전송중 오류발생: " + e.getClass().getName() + ", " + e.getMessage());
                        }

                    }
            );
        });

        connManager.setOnHotspotUnAvailable(() -> {
            connManager.setStatusText("핫스팟 연결 실패, 또는 등록모드가 아님");
            System.out.println("핫스팟 연결 실패, 또는 등록모드가 아님");
        });


        connManager.setOnHotspotLost(() -> {
            connManager.setStatusText("핫스팟 연결 끊김");
            System.out.println("핫스팟 연결 끊김");
        });


        connManager.setOnExternalAvailable(() -> {
            connManager.setStatusText("외부 와이파이 연결 성공");
            System.out.println("외부 와이파이 연결 성공");

            connManager.listenAndACKtoUDP(30000); //타임아웃 30초
        });

        connManager.setOnExternalUnAvailable(() -> {
            connManager.setStatusText("외부 와이파이 연결 실패");
            System.out.println("외부 와이파이 연결 실패");
        });

        connManager.setOnExternalLost(() -> {
            connManager.setStatusText("외부 와이파이 연결 끊김");
            System.out.println("외부 와이파이 연결 끊김");
        });

        connManager.setOnUdpSuccess(() -> {
            connManager.setStatusText("UDP 프로세스 성공");
            System.out.println("UDP 프로세스 성공");

            EXTERNAL_IP = connManager.arduinoIP; //UDP가 성공하면 아두이노 웹서버 IP 가져올수있음

            connManager.sendPing(30000, EXTERNAL_IP);  //타임아웃 30초
        });

        connManager.setOnUdpTimeout(() -> {
            connManager.setStatusText("UDP 프로세스 타임아웃");
            System.out.println("UDP 프로세스 타임아웃");
        });

        connManager.setOnUdpError(() -> {
            connManager.setStatusText("UDP 프로세스 오류");

            Exception e = connManager.getCurrentException();  //오류 가져오기
            System.out.println("UDP 프로세스 오류: " + e.getClass().getName() + ", " + e.getMessage());
        });

        connManager.setOnPingSuccess(() -> {
            connManager.setStatusText("핑 보내기 성공");
            System.out.println("핑 보내기 성공");

            CONNECT_STATE = true;   //모든 절차가 끝났으므로 연결상태를 True로 바꿔주기
            EXTERNAL_SSID = ssidEditText.getText().toString().trim();
            EXTERNAL_PW = passwordEditText.getText().toString().trim();

            try {
                Thread.sleep(2000);
                connManager.setStatusText("최종 연결 성공!");
                System.out.println("최종 연결 성공!");
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        connManager.setOnPingFailed(() -> {
            CONNECT_STATE = false;

            connManager.setStatusText("핑 보내기 실패");
            System.out.println("핑 보내기 실패");
        });

        connManager.setOnPingTimeout(() -> {
            CONNECT_STATE = false;

            connManager.setStatusText("핑 보내기 타임아웃");
            System.out.println("핑 보내기 타임아웃");
        });

        connManager.setOnPingError(() -> {
            CONNECT_STATE = false;

            connManager.setStatusText("핑 보내기 오류");

            Exception e = connManager.getCurrentException();  //오류 가져오기
            System.out.println("핑 보내기 오류: " + e.getClass().getName() + ", " + e.getMessage());
        });

        //테스트 데이터 가져오기 예제
        requestDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("EXTERNAL_SSID: " + EXTERNAL_SSID);
                System.out.println("EXTERNAL_PW: " + EXTERNAL_PW);
                System.out.println("EXTERNAL_IP: " + EXTERNAL_IP);

                new Thread(() -> {
                    if (connManager.sendPing(5000, EXTERNAL_IP)) {   //핑이 성공하면, rememberedAruduinoIP는 저장해둔 아이피주소
                        try {
                            URL url = new URL("http://" + EXTERNAL_IP + ":12345/testData"); //저장해둔 IP주소를 향해 데이터요청

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(10000);
                            connection.connect();

                            final int responseCode = connection.getResponseCode();

                            if (responseCode == HttpURLConnection.HTTP_OK) { //응답이 성공하면 데이터 가져오기
                                InputStream inputStream = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                reader.close();
                                inputStream.close();

                                // 데이터 출력
                                String responseData = response.toString();
                                connManager.setStatusText("데이터 가져옴: " + responseData);
                            } else {
                                connManager.setStatusText("데이터 가져오기 실패");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {  //rememberedArduinoIP를 통해 연결이 안되었으므로
                        connManager.setStatusText("연결이 올바르지 않음");

                        //저장해둔 외부네트워크의 ssid, pw를 사용해서 연결프로세스 다시 실행
                        startConnect();
                    }
                }).start();
            }
        });
    }

}
