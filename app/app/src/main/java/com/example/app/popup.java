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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class popup extends AppCompatActivity implements View.OnClickListener {
        public static String ssid;
        public static String pw;
        public static String ip;
        public static String plant;
        public static TextView connText;
        private int responseCode;
        public static boolean connCheck;
        public static boolean ipGetCheck;
        private Context context;

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

        editPlant.setText(plant); //과거에 저장된값이 있다면 실행시 보여주기
        editSsid.setText(ssid);
        editPw.setText(pw);
        regiBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //getText();              //입력값 SSID,PW 가져오기
                //register(ssid, pw);     //아두이노로 SSID,PW넘겨주고 아두이노의 접속확인까지
                webCheck();
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


    public void getText(){
        ssid=pw=ip=null;
        EditText editSsid = findViewById(R.id.in_ssid);
        EditText editPw = findViewById(R.id.in_pw);
        ssid = editSsid.getText().toString();
        pw = editPw.getText().toString();
        Toast.makeText(getApplicationContext(), "입력값 가져오기 성공", Toast.LENGTH_SHORT).show();
    }
    public void register(String SSID, String PW){
        connText.setText("연결중");
        responseCode = 0;
        connCheck = false;
        ipGetCheck= false;

        RegisterRunnable registerRunnable = new RegisterRunnable(SSID, PW);
        Thread registerThread = new Thread(registerRunnable);
        registerThread.start();
    }
    public void webCheck(){
        WebCheckRunnable webCheckRunnable = new WebCheckRunnable();
        Thread webCheckThread = new Thread(webCheckRunnable);
        webCheckThread.start();
    }
    private class RegisterRunnable implements Runnable {
        private String ssid;
        private String pw;

        public RegisterRunnable(String ssid, String pw) {
            this.ssid = ssid;
            this.pw = pw;
        }

        @Override
        public void run() {
            try {
                // URL 생성
                String urlString = "http://192.168.4.1:12344/regExtWifi?ssid=" + URLEncoder.encode(ssid, "UTF-8") + "&pw=" + URLEncoder.encode(pw, "UTF-8");
                URL url = new URL(urlString);

                // HTTP GET 요청 설정
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10초 동안 연결 시도
                connection.setReadTimeout(30000);
                // 응답 코드 확인
                responseCode = connection.getResponseCode();
                /*
                while(responseCode != HttpURLConnection.HTTP_OK) {

                    Thread.sleep(100);
                    System.out.println("...");
                }*/
                //Log.d(TAG, "Response code: " + responseCode);
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception in register: " + e.getMessage());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (responseCode == 200) {
                        System.out.println("아두이노 연결성공");
                        connText.setText("아두이노가 " + ssid + "에 연결성공");

                    }
                    else {
                        System.out.println("정보 확인 후 다시 등록");
                        connText.setText("정보 확인 후 다시 등록");
                    }
                        //여기까지 테스트 성공
                    if (responseCode == 200) {
                        System.out.println("와이파이변경코드 직전");
                        //popup popupObj = new popup();
                        //popupObj.connText = (TextView) findViewById(R.id.show_conn);
                        WifiConnector wifiConnector = new WifiConnector(context);
                        wifiConnector.connectToNetwork(ssid, pw, new WifiConnector.ConnectionCallback() {
                            @Override
                            public void onConnectionResult(boolean isConnected) {
                                if (isConnected) {
                                    System.out.println("Successfully connected to the network");
                                } else {
                                    System.out.println("Failed to connect to the network");
                                }
                            }
                        });
                    }

                    if (connCheck) {
                        Runnable udpClientRunnable = new UdpClientRunnable(context);
                        Thread udpClientThread = new Thread(udpClientRunnable);
                        udpClientThread.start();
                    }

                    if (ipGetCheck) {
                        webCheck();
                    }
                }
            });
        }
    }
    private class WebCheckRunnable implements Runnable {
        @Override
        public void run() {
            try {
                // URL 생성
                String urlString = "http://" + ip + ":12345/testData";
                URL url = new URL(urlString);
                System.out.println("URL생성");
                // HTTP GET 요청 설정
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10초 동안 연결 시도

                // 응답 코드 확인
                responseCode = connection.getResponseCode();
                System.out.println(responseCode);
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception in Web: " + e.getMessage());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (responseCode == 200) {
                        connText.setText("최종접속성공");
                        // 값을 저장할 SharedPreferences 객체 생성
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        // 값을 편집하기 위한 Editor 객체 생성
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("ssid", ssid);
                        editor.putString("pw", pw);
                        editor.putString("ip", ip);
                        editor.apply();
                    } else {
                        connText.setText("최종실패, 다시등록바랍니다");
                    }
                }
            });
        }
    }
}