package com.example.app;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class popup extends AppCompatActivity implements View.OnClickListener {
        public static boolean CONNECT_STATE = false;    
        public static String ssid;
        public static String pw;
        public static String ip;
        public static String url;
        public static String plant;
        public static TextView connText;
        private Context context;
        WifiConnectionManager connManager;
        SeekBar lightSeekBar;
        SeekBar coolSeekBar;
        Button sendButton;
        Button receiveButton;
        int lightProgress, coolProgress, waterProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.activity_popup);
        findViewById(R.id.btnClose).setOnClickListener((View.OnClickListener) this);
        Button regiBtn = findViewById(R.id.btnregister);
        Button plantBtn = findViewById(R.id.btnPlant);
        lightSeekBar = findViewById(R.id.seekBar);
        coolSeekBar = findViewById(R.id.seekBar2);
        receiveButton = findViewById(R.id.receivebutton);
        sendButton = findViewById(R.id.sendbutton);
        connText = findViewById(R.id.show_conn);

        EditText editPlant = findViewById(R.id.plantName);
        EditText editSsid = findViewById(R.id.in_ssid);
        EditText editPw = findViewById(R.id.in_pw);

        editPlant.setText(plant); 
        editSsid.setText(ssid);
        editPw.setText(pw);

        
        connManager = new WifiConnectionManager(this, this.connText);
        regiBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                
                connManager.setStatusText("SSID 및 비밀번호를 입력하세요");
                 new Thread(()->{
                     startConnect();
                 }).start();
                
                connManager.setOnHotspotAvailable(() -> {
                    connManager.setStatusText("핫스팟 이용가능, 아두이노에게 전송 시작함...");
                    String externalSSID = editSsid.getText().toString().trim();
                    String externalPW = editPw.getText().toString().trim();
                    
                    connManager.sendExternalWifiInfo(
                            externalSSID,
                            externalPW,
                            30000,  
                            () -> {    
                                connManager.setStatusText("아두이노가 외부네트워크에 연결됨");
                                
                                int externalTimeout = 30000;    
                                connManager.connectToExternal(externalSSID, externalPW, externalTimeout);
                            },
                            () -> {connManager.setStatusText("아두이노가 외부네트워크에 연결을 실패함");},
                            () -> {    
                                Exception e = connManager.getCurrentException();  
                                if (e instanceof SocketTimeoutException)
                                    connManager.setStatusText("아두이노에게 정보전송 타임아웃");
                                else if (e instanceof SocketException)         
                                    connManager.setStatusText("아두이노에게 정보전송 중 연결 실패오류");
                                else
                                    connManager.setStatusText("아두이노에게 정보전송중 오류발생");
                            }
                    );
                });
                connManager.setOnHotspotUnAvailable(() -> {
                    connManager.setStatusText("핫스팟 연결 실패, 또는 등록모드가 아님");
                });
                connManager.setOnHotspotLost(() -> {
                    connManager.setStatusText("핫스팟 연결 끊김");
                });
                connManager.setOnExternalAvailable(() -> {
                    connManager.setStatusText("외부 와이파이 연결 성공");
                    connManager.listenAndACKtoUDP(30000); 
                });
                connManager.setOnExternalUnAvailable(() -> {
                    connManager.setStatusText("외부 와이파이 연결 실패");
                });
                connManager.setOnExternalLost(() -> {
                    connManager.setStatusText("외부 와이파이 연결 끊김");
                });
                connManager.setOnUdpSuccess(() -> {
                    connManager.setStatusText("UDP 프로세스 성공");
                    ip = connManager.arduinoIP; 
                    url = "http://" + ip + ":12345/";
                    connManager.sendPing(30000, ip);  
                });
                connManager.setOnUdpTimeout(() -> {
                    connManager.setStatusText("UDP 프로세스 타임아웃");
                });
                connManager.setOnUdpError(() -> {
                    connManager.setStatusText("UDP 프로세스 오류");
                });
                connManager.setOnPingSuccess(() -> {
                    connManager.setStatusText("핑 보내기 성공");
                    CONNECT_STATE = true;   
                    ssid = editSsid.getText().toString().trim();
                    pw = editPw.getText().toString().trim();
                    try {
                        Thread.sleep(2000);
                        connManager.setStatusText("최종 연결 성공!");
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
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
                });

                connManager.setOnPingTimeout(() -> {
                    CONNECT_STATE = false;
                    connManager.setStatusText("핑 보내기 타임아웃");
                });

                connManager.setOnPingError(() -> {
                    CONNECT_STATE = false;
                    connManager.setStatusText("핑 보내기 오류");
                });
            }
        });
        plantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plant = editPlant.getText().toString();
                
                
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                
                SharedPreferences.Editor editor = sharedPreferences.edit();
                
                editor.putString("plant", plant);
                editor.apply();
                Toast.makeText(getApplicationContext(), "식물 등록 성공", Toast.LENGTH_LONG).show(); 
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { 
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(popup.url + "getTableData?name=manage_auto");
                            HttpURLConnection urlConnection =urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.connect();
                            InputStream inputStream = urlConnection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line;
                            if (inputStream == null)
                                return;
                            StringBuilder responseData = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                responseData.append(line);
                            }
                            reader.close();
                            inputStream.close();
                            urlConnection.disconnect();
                            String parsed[] = responseData.toString().split("\\|");
                            if (parsed[0].equals("ok") && parsed[1].equals("0")) {
                                String dataString = parsed[2];
                                JSONArray jsonArray = new JSONArray(dataString);
                                if (jsonArray.length() > 0) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    lightProgress = jsonObject.getInt("ld");
                                    coolProgress = jsonObject.getInt("cd");
                                    save(lightProgress, coolProgress);
                                }
                            }
                            runOnUiThread(new Runnable() { 
                                @Override
                                public void run() {
                                    lightSeekBar.setProgress(lightProgress);
                                    coolSeekBar.setProgress(coolProgress);
                                }
                            });

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                lightProgress = lightSeekBar.getProgress();
                coolProgress = coolSeekBar.getProgress();

                String sendUrlString = url + "manageAutoSet?ld=" + lightProgress + "&cd=" + coolProgress;
                new Thread(new Runnable() { 
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            URL url = new URL(sendUrlString);
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            int responseCode = urlConnection.getResponseCode();
                            if(responseCode == HttpURLConnection.HTTP_OK)
                                save(lightProgress, coolProgress);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect(); 
                            }
                        }
                    }
                }).start();
            }
        });

    }
    @Override
    public void onResume() { 
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        lightProgress = sharedPreferences.getInt("lightProgress", 50); 
        coolProgress = sharedPreferences.getInt("coolProgress", 50); 
        waterProgress = sharedPreferences.getInt("waterProgress", 50); 
        lightSeekBar.setProgress(lightProgress);
        coolSeekBar.setProgress(coolProgress);
    }
    public void save(int lightProgress, int coolProgress){ 
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lightProgress", lightProgress);
        editor.putInt("coolProgress", coolProgress);
        editor.apply();
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
    }  

    public void startConnect() {
        CONNECT_STATE = false;
        ip = null;
        int hotspotTimeout = 30000; 
        connManager.connectToHotspot("SmartPotModule", hotspotTimeout);
    }
}