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
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    public static boolean CONNECT_STATE = false;
    private EditText ssidEditText;
    private EditText passwordEditText;
    private Button connectButton;
    private Button requestDataButton;
    private TextView statusLabel;

    WifiConnectionManager connManager;

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
         * xyz.cofon.wificonnectapp system.out
         * 로그캣 System out 필터
         * */

        //WifiConnectManager 객체 생성 후 activity 인스턴스와 상태표시 Textview 넘겨주기
        connManager = new WifiConnectionManager(this, this.statusLabel);
        connManager.setStatusText("SSID 및 비밀번호를 입력하세요");
        System.out.println("SSID 및 비밀번호를 입력하세요");


        //핫스팟이 연결되면 실행될 콜백 정의
        connManager.setOnHotspotAvailable(() -> {
            connManager.setStatusText("핫스팟에 연결 성공");
            System.out.println("핫스팟에 연결 성공");

            String externalSSID = ssidEditText.getText().toString().trim();
            String externalPW = passwordEditText.getText().toString().trim();

            //연결 성공되었으므로 외부네트워크 정보를 아두이노에 전송함
            connManager.sendExternalWifiInfo(
                    externalSSID,
                    externalPW,
                    30000,  //타임아웃
                    () -> {    // 성공시 콜백
                        connManager.setStatusText("아두이노에 정보전송 성공");
                        System.out.println("아두이노에 정보전송 성공");

                        //외부네트워크로 변경해주기
                        connManager.connectToExternal(externalSSID, externalPW);
                    },
                    () -> {    // 실패시 콜백
                        connManager.setStatusText("아두이노에 정보전송 실패");
                        System.out.println("아두이노에 정보전송 실패");
                    },
                    () -> {    // 에러시 콜백
                        connManager.setStatusText("아두이노에 정보전송 오류");
                        System.out.println("아두이노에 정보전송 오류");
                    }
            );
        });

        connManager.setOnHotspotUnAvailable(() -> {
            connManager.setStatusText("핫스팟 연결 실패");
            System.out.println("핫스팟 연결 실패");
        });


        connManager.setOnHotspotLost(() -> {
            connManager.setStatusText("핫스팟 연결 끊김");
            System.out.println("핫스팟 연결 성공");
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
            connManager.sendWebmessage(30000);  //타임아웃 30초
        });

        connManager.setOnUdpTimeout(() -> {
            connManager.setStatusText("UDP 프로세스 타임아웃");
            System.out.println("UDP 프로세스 타임아웃");
        });

        connManager.setOnSendWebMessageSuccess(() -> {
            connManager.setStatusText("WebMsg 성공");
            System.out.println("WebMsg 성공");

            CONNECT_STATE = true;   //모든 절차가 끝났으므로 연결상태를 True로 바꿔주기

            try {
                Thread.sleep(2000);
                connManager.setStatusText("최종 연결 성공!");
                System.out.println("최종 연결 성공!");
            }catch(Exception e){
                e.printStackTrace();
            }

        });

        connManager.setOnSendWebMessageFailed(() -> {
            connManager.setStatusText("WebMsg 타임아웃");
            System.out.println("WebMsg 타임아웃");
        });


        //연결 버튼 리스너 등록
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!connManager.permission.hasAll()) {
                    connManager.permission.requestAll();
                }
                connManager.connectToHotspot("SmartPotModule");
            }
        });


        //테스트 데이터 가져오기 예제
        requestDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    if (CONNECT_STATE) {
                        try {
                            URL url = new URL("http://" + connManager.arduinoIP + ":12345/testData");

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
                    } else {  //CONNECT_STATE가 아직 true가아니면 연결이 안되었으므로
                        connManager.setStatusText("아직 연결을 안했음");
                    }
                }).start();
            }
        });
    }

}
