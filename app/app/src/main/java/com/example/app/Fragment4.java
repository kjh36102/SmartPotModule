package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.fragment.app.Fragment;

import org.json.JSONException;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Fragment4 extends Fragment {
    SeekBar lightSeekBar;
    SeekBar coolSeekBar;
    SeekBar waterSeekBar;
    Button sendButton;
    Button receiveButton;
    int lightProgress, coolProgress, waterProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_4, container, false);
        lightSeekBar = view.findViewById(R.id.seekBar);
        coolSeekBar = view.findViewById(R.id.seekBar2);
        waterSeekBar = view.findViewById(R.id.seekBar3);

        receiveButton = view.findViewById(R.id.receivebutton);
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //데이터 값 받는 버튼
                
                new Thread(new Runnable() {
                    @Override
                    public void run() { //http통신으로 받아옴
                        HttpURLConnection urlConnection = null;
                        BufferedReader reader = null;

                        try {
//                            URL url = new URL("http://arduino.ip:12345/getTableData?name=manage_auto"); //컬럼 다 넘겨주나보네
                            URL url = new URL("http://cofon.xyz:9090/read?col=temp_humid"); //sample data test
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.connect();

                            InputStream inputStream = urlConnection.getInputStream();
                            StringBuffer buffer = new StringBuffer();

                            if (inputStream == null) {
                                System.out.println("inputStream ㅜull");
                                return;
                            }

                            reader = new BufferedReader(new InputStreamReader(inputStream));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                buffer.append(line + "\n");
                            }

                            if (buffer.length() == 0) {
                                System.out.println("받은 값 길이가 0");
                                return;
                            }
                            //만약 [ { ... } ] 형식이면 아래 코드
//                            JSONArray jsonArray = new JSONArray(buffer.toString());
//                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            // { ... } 형식이면 아래 코드
                            JSONObject jsonObject = new JSONObject(buffer.toString());
                            System.out.println("json 상태: "+ jsonObject);

                            //json 데이터 전처리
//                            lightProgress = jsonObject.getInt("ld");
//                            coolProgress = jsonObject.getInt("cd");
//                            waterProgress = jsonObject.getInt("ot");
//                            System.out.println("ld: " + lightProgress);
//                            System.out.println("cd: " + coolProgress);
//                            System.out.println("ot: " + waterProgress);

                            
                            //sample data test
                            lightProgress = jsonObject.getInt("ec");
                            coolProgress = jsonObject.getInt("lt");
                            waterProgress = jsonObject.getInt("n");
                            System.out.println("ec: " + lightProgress);
                            System.out.println("lt: " + coolProgress);
                            System.out.println("n: " + waterProgress);

                            //저장
                            save(lightProgress, coolProgress, waterProgress);

                            getActivity().runOnUiThread(new Runnable() { //받아온 데이터 값 ui 적용
                                @Override
                                public void run() {
                                    lightSeekBar.setProgress(lightProgress);
                                    coolSeekBar.setProgress(coolProgress);
                                    waterSeekBar.setProgress(waterProgress);
                                }
                            });


                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }

                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            }
        });

        sendButton = view.findViewById(R.id.sendbutton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //설정한 progrss bar 값 읽는거
                lightProgress = lightSeekBar.getProgress();
                coolProgress = coolSeekBar.getProgress();
                waterProgress = waterSeekBar.getProgress();

                System.out.println("lightProgress: " + lightProgress);
                System.out.println("coolProgress: " + coolProgress);
                System.out.println("waterProgress: " + waterProgress);


//                String sendUrlString = "http://arduino.ip:12345/manageAutoSet?ld=" + lightProgress
//                        + "&cd=" + coolProgress;

                String sendUrlString= "https://zmzlqay.request.dreamhack.games"; //sample test

                new Thread(new Runnable() { //http 통신 시작
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            URL url = new URL(sendUrlString);
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            int responseCode = urlConnection.getResponseCode();

                            if(responseCode == HttpURLConnection.HTTP_OK){
                                System.out.println("progress bar data 보내기 성공");
                                save(lightProgress, coolProgress, waterProgress); //이 코드는 고민

                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect(); //http 연결 종ㅇㅇ료
                            }
                        }
                    }
                }).start();
            }
        });
        return view;
    }
    
    @Override
    public void onResume() { //새로고침 할 떄마다 로컬저장소에 있는 데이터 불러와서 ui 적용
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        lightProgress = sharedPreferences.getInt("lightProgress", 50); // Default value is 50
        coolProgress = sharedPreferences.getInt("coolProgress", 50); // Default value is 50
        waterProgress = sharedPreferences.getInt("waterProgress", 50); // Default value is 50

        lightSeekBar.setProgress(lightProgress);
        coolSeekBar.setProgress(coolProgress);
        waterSeekBar.setProgress(waterProgress);
    }
    public void save(int lightProgress, int coolProgress, int waterProgress){ //값 저장
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("lightProgress", lightProgress);
        editor.putInt("coolProgress", coolProgress);
        editor.putInt("waterProgress", waterProgress);
        editor.apply();
    }
}