package com.example.app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

public class popup extends AppCompatActivity implements View.OnClickListener {
        public static boolean CONNECT_STATE = false;    //현재 아두이노와의 연결상태
        public static String ssid;
        public static String pw;
        public static String ip;
        public static String url;
        public static String plant;
        public static TextView connText;
        private Context context;
        WifiConnectionManager connManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //타이틀 미사용
        setContentView(R.layout.activity_popup);
        findViewById(R.id.btnClose).setOnClickListener((View.OnClickListener) this);
        Button regiBtn = findViewById(R.id.btnregister);
        Button plantBtn = findViewById(R.id.btnPlant);
        connText = findViewById(R.id.show_conn);

        EditText editPlant = findViewById(R.id.plantName);
        EditText editSsid = findViewById(R.id.in_ssid);
        EditText editPw = findViewById(R.id.in_pw);
        //Toast.makeText(getApplicationContext(), "입력값 가져오기 성공", Toast.LENGTH_SHORT).show(); 테스트코드

        editPlant.setText(plant); //과거에 저장된값이 있다면 실행시 보여주기
        editSsid.setText(ssid);
        editPw.setText(pw);
        //WifiConnectManager 객체 생성 후 activity 인스턴스와 상태표시 Textview 넘겨주기
        connManager = new WifiConnectionManager(this, this.connText);
        regiBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //처음 안내
                connManager.setStatusText("SSID 및 비밀번호를 입력하세요");
                System.out.println("SSID 및 비밀번호를 입력하세요");
                 new Thread(()->{
                     startConnect();

                 }).start();
                //핫스팟이 연결되면 실행될 콜백 정의
                connManager.setOnHotspotAvailable(() -> {
                    connManager.setStatusText("핫스팟 이용가능, 아두이노에게 전송 시작함...");
                    System.out.println("핫스팟 이용가능, 아두이노에게 전송 시작함...");

                    String externalSSID = editSsid.getText().toString().trim();
                    String externalPW = editPw.getText().toString().trim();

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
                    ip = connManager.arduinoIP; //UDP가 성공하면 아두이노 웹서버 IP 가져올수있음
                    url = "http://" + ip + ":12345/read";
                    connManager.sendPing(30000, ip);  //타임아웃 30초
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
                    ssid = editSsid.getText().toString().trim();
                    pw = editPw.getText().toString().trim();

                    try {
                        Thread.sleep(2000);
                        connManager.setStatusText("최종 연결 성공!");
                        System.out.println("최종 연결 성공!");

                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        // 값을 편집하기 위한 Editor 객체 생성
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("ssid", ssid);
                        editor.putString("pw",pw);
                        editor.putString("ip",ip);
                        editor.putString("url", url);
                        editor.apply();
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

            }
        });
        plantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plant = editPlant.getText().toString();
                //plant명 앱내에 저장
                // 값을 저장할 SharedPreferences 객체 생성
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                // 값을 편집하기 위한 Editor 객체 생성
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // 값을 SharedPreferences에 저장
                editor.putString("plant", plant);
                editor.apply();
                Toast.makeText(getApplicationContext(), "식물 등록 성공", Toast.LENGTH_LONG).show(); //토스트메시지 표시
            }
        });
    }
    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnClose:
                this.finish();
                break;
        }
    }
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {return false;}
        return true;
    }  //팝업 영역 밖 클릭시 닫힘 방지

    public void startConnect() {
        CONNECT_STATE = false;
        ip = null;
        int hotspotTimeout = 30000; //연결을 수락하고 최종적으로 연결될때까지의 시간, 따라서 넉넉히 줘야함
        connManager.connectToHotspot("SmartPotModule", hotspotTimeout);
    }
}