package com.example.app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        editPlant.setText(plant); //과거에 저장된값이 있다면 실행시 보여주기
        regiBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getText();
                register(ssid, pw);
                if (responseCode == 200) {
                    WifiUtils wifiUtils = new WifiUtils(getApplicationContext());
                    boolean isConnected = wifiUtils.connectToWifi(ssid, pw);
                    if (isConnected) {
                        // Wi-Fi 연결 성공
                        connText.setText("앱이 "+ ssid + "에 연결됨");
                        connCheck=true;
                    } else {
                        // Wi-Fi 연결 실패
                        connText.setText("앱이 "+ ssid + "에 연결실패");
                    }
                if(connCheck==true){     //와아파이에 연결 성공시
                    Runnable udpClientRunnable = new UdpClientRunnable(context); //udp로 ip저
                    Thread udpClientThread = new Thread(udpClientRunnable);
                    udpClientThread.start();
                }
                webCheck();   //웹서버 열렸는지 접속확인
                }
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
                editor.putString("userInput", plant);
                editor.apply();
                Toast.makeText(getApplicationContext(), "식물 등록 성공", Toast.LENGTH_SHORT).show(); //토스트메시지 표시
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
    }
    public void register(String SSID, String PW){
        connText.setText("연결중");
        responseCode=0;
        connCheck=false;
        try {
            // URL 생성
            String urlString = "http://" + "192.168.0.1:" + 12345 + "/?action=regExtWifi&ssid=" + URLEncoder.encode(ssid, "UTF-8") + "&pw=" + URLEncoder.encode(pw, "UTF-8");
            URL url = new URL(urlString);

            // HTTP GET 요청 설정
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10초 동안 연결 시도
            connection.setReadTimeout(5000);
            // 응답 코드 확인
            responseCode = connection.getResponseCode();
            Log.d(TAG, "Response code: " + responseCode);

            if(responseCode == 200)
                connText.setText("아두이노가 " + ssid +"에 연결성공");
            else
                connText.setText("정보 확인 후 다시 등록");

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void webCheck(){
        try {
            // URL 생성
            String urlString = "http://" + ip + ":" + 12345 + "/?action=hello";
            URL url = new URL(urlString);

            // HTTP GET 요청 설정
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10초 동안 연결 시도

            // 응답 코드 확인
            responseCode = connection.getResponseCode();
            if(responseCode==200)
                connText.setText("최종접속성공");
            else
                connText.setText("최종실패, 다시등록바랍니다");

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}