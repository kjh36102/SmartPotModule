package com.example.app;

import static com.example.app.Fragment1.angryface;
import static com.example.app.Fragment1.noface;
import static com.example.app.Fragment1.score;
import static com.example.app.Fragment1.smileface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import com.example.app.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    TabPagerAdapter adapter;
    AppCompatButton water;
    ToggleButton toggleButton;

    String[] tabName = new String[]{"대시보드", "상세분석", "식물관리"};
    //습도(humid), 온도(temp), 전기전도도(ec), 산화도(ph), 질소(nitro), 인(phos), 칼륨(pota), 광량(light);
    public static HashMap<String, String> mDataHashMap;
    private static final int PERMISSION_REQUEST_CODE = 0;
    public static SharedPreferences sharedPreferences_fragment2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 앱 권한 동의 요청
        requestPermissions();

        sharedPreferences_fragment2 = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);           // actionbar에서 toolbar로 변경
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle("SmartPotModule");
        //xml 연결
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);
        //adapter 준비 및 연결
        adapter = new TabPagerAdapter(this);
        viewPager.setAdapter(adapter);

        water = findViewById(R.id.nowWater);
        toggleButton = findViewById(R.id.toggleButton);


        // TabLayout, ViewPager 연결
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                TextView textView = new TextView(MainActivity.this);
                textView.setText(tabName[position]);
                textView.setTextSize(18);
                textView.setGravity(Gravity.CENTER);
                tab.setCustomView(textView);
            }
        }).attach();

        findViewById(R.id.wifi_button).setOnClickListener(this);
        //조명상태 불러오는 코드 추가해야함

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        popup.plant = sharedPreferences.getString("plant", ""); // 두 번째 매개변수는 기본값으로 사용될 값입니다.
        popup.ssid = sharedPreferences.getString("ssid", "");
        popup.pw = sharedPreferences.getString("pw", "");
        popup.ip = sharedPreferences.getString("ip", "");
        //popup.url = sharedPreferences.getString("url", "");

        new GetJsonDataTask().execute(popup.url);
        /*
        WifiConnectionManager connManager = new WifiConnectionManager(this, popup.connText);
        if (!connManager.permission.hasAll())
            connManager.permission.requestAll();
        if(!popup.ssid.equals("")&& !popup.pw.equals("") && !popup.ip.equals("") && !popup.url+"getTableData?name=soil_data".equals("")){
            new Thread(()->{
                connManager.connectToExternal(popup.ssid,popup.pw, 30000);
            }).start();
            connManager.setOnExternalAvailable(() -> {
                System.out.println("외부 와이파이 연결 성공");      //아두이노 접속 테스트
                    new Thread(() -> {
                        if (connManager.sendPing(5000, popup.ip)) {   //핑이 성공하면, rememberedAruduinoIP는 저장해둔 아이피주소
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "서버 연결 성공", Toast.LENGTH_SHORT).show();
                                }
                            });
                            System.out.println(popup.ip + " " + popup.url);
                            new GetJsonDataTask().execute(popup.url+"getTableData?name=soil_data");
                            new Thread(()->{
                                try {
                                    while(score != -1) {
                                        Thread.sleep(100);
                                    }
                                    setFace();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }).start();
                        }
                    }).start();
            });
            connManager.setOnExternalUnAvailable(() -> {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "제품 등록 바람", Toast.LENGTH_SHORT).show();
                    }
                });
                System.out.println("제품 등록 바람");
            });
        }
            else {  //rememberedArduinoIP를 통해 연결이 안되었으므로
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "재등록 바람", Toast.LENGTH_SHORT).show();
                    }
                });
                setBlackFace();
            }
            */


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_button:
                startActivity(new Intent(this, popup.class));
                break;
        }
    }   //wifi버튼-페이지 연결

    public class GetJsonDataTask extends AsyncTask<String, Void, HashMap<String, String>> {
        @Override
        protected HashMap<String, String> doInBackground(String... urls) {
            mDataHashMap=null;
            HashMap<String, String> resultHashMap = new HashMap<>();
            try {
                URL url = new URL(urls[0]);
                //URL url = new URL(urls[0]+"getTableData?name=soil_data");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                resultHashMap.put("temp", jsonObject.getString("tm"));
                resultHashMap.put("humid", jsonObject.getString("hm"));
                resultHashMap.put("light", jsonObject.getString("lt"));
                resultHashMap.put("ph", jsonObject.getString("ph"));
                resultHashMap.put("nitro", jsonObject.getString("n"));
                resultHashMap.put("phos", jsonObject.getString("p"));
                resultHashMap.put("pota", jsonObject.getString("k"));
                resultHashMap.put("ec", jsonObject.getString("ec"));
                //resultHashMap.put("ts", jsonObject.getString("ts"));

                URL url2 = new URL(urls[0] + "getTableData?name=plant_manage");
                HttpURLConnection httpURLConnection2 = (HttpURLConnection) url2.openConnection();
                InputStream inputStream2 = httpURLConnection2.getInputStream();
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                String line2;
                StringBuilder stringBuilder2 = new StringBuilder();
                while ((line2 = bufferedReader2.readLine()) != null) {
                    stringBuilder2.append(line2);
                }
                bufferedReader2.close();
                inputStream2.close();
                httpURLConnection2.disconnect();

                JSONObject jsonObject2 = new JSONObject(stringBuilder2.toString());
                if(jsonObject.getInt("w_auto") == 0)
                    water.setEnabled(true);
                else if(jsonObject.getInt("w_auto") == 1)
                    water.setEnabled(false);

                if(jsonObject.getInt("l_auto") == 0) {
                    toggleButton.setEnabled(true);
                    if(jsonObject.getInt("l_on") == 1)
                        toggleButton.setChecked(true);
                    else if (jsonObject.getInt("l_on") == 0)
                        toggleButton.setChecked(false);
                }
                else if(jsonObject.getInt("l_auto") == 1)
                    water.setEnabled(false);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultHashMap;
        }
        @Override
        public void onPostExecute(HashMap<String, String> resultHashMap) {
            mDataHashMap = resultHashMap;
            updateDataTextView();
        }
    }

    private void updateDataTextView() {
        if (mDataHashMap != null) {
            String temp = mDataHashMap.get("temp");
            String humid = mDataHashMap.get("humid");
            String light = mDataHashMap.get("light");
            String ph = mDataHashMap.get("ph");
            String nitro = mDataHashMap.get("nitro");
            String phos = mDataHashMap.get("phos");
            String pota = mDataHashMap.get("pota");
            String ec = mDataHashMap.get("ec");
            String ts = mDataHashMap.get("ts");
            TextView tempText = findViewById(R.id.temp);
            TextView humidText = findViewById(R.id.humid);
            TextView lightText = findViewById(R.id.light);
            TextView phText = findViewById(R.id.ph);
            TextView nitroText =findViewById(R.id.nitro);
            TextView phosText = findViewById(R.id.phos);
            TextView potaText =findViewById(R.id.pota);
            TextView ecText = findViewById(R.id.ec);
            TextView rTxt = findViewById(R.id.rText);
            tempText.setText(temp);
            humidText.setText(humid);
            lightText.setText(light);
            phText.setText(ph);
            nitroText.setText(nitro);
            phosText.setText(phos);
            potaText.setText(pota);
            ecText.setText(ec);
            //rTxt.setText(ts);  서버의 업데이트시간 불러오기
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat=new SimpleDateFormat("마지막 업데이트 시간 : yyyy-MM-dd_HH:mm");
            String dateTime = dateFormat.format(calendar.getTime());
            rTxt.setText(dateTime);

            Fragment2.temp = temp;
            Fragment2.humid = humid;
            Fragment2.light = light;
            Fragment2.nitro = nitro;
            Fragment2.phos = phos;
            Fragment2.pota = pota;
            Fragment2.ec = ec;
            Fragment2.ph = ph;


            Fragment3.humid = humid;
            Fragment3.light = light;

            ChatGPT chatGPT = new ChatGPT();
            new Thread(){
                public void run(){
                    org.json.simple.JSONObject scoreResponse = chatGPT.score(popup.plant, Double.parseDouble(temp),  Double.parseDouble(humid),  Double.parseDouble(nitro),  Double.parseDouble(phos),  Double.parseDouble(pota),  Double.parseDouble(ph),  Double.parseDouble(ec),  Double.parseDouble(light));
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(scoreResponse);
                                String scoreString = json.getString("총점");
                                score = Float.parseFloat(scoreString);
                                setFace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }.start();
        }
    }
    public void setBlackFace(){
        setBlackImage(smileface, R.drawable.smileface1);
        setBlackImage(noface, R.drawable.noface1);
        setBlackImage(angryface, R.drawable.angryface1);
    }
    public void setFace(){
        setBlackFace();
        try{
            if(score >=80)
                setColorImage(smileface, R.drawable.smileface);
            else if(score >=50)
                setColorImage(noface, R.drawable.noface);
            else if(score >1)
                setColorImage(angryface, R.drawable.angryface);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setBlackImage(ImageView imageView, int resourceId) {
        if (imageView != null) {
            Resources resources = getResources();
            Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);
            BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
            imageView.setImageDrawable(drawable);
            System.out.println(imageView);
        }
    }

    public void setColorImage(ImageView imageView, int resourceId) {
        if (imageView != null) {
            Resources resources = getResources();
            Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);
            BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
            imageView.setImageDrawable(drawable);
        }
    }


    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

            boolean allPermissionsGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 권한 동의 결과 처리
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // 필요한 권한이 모두 동의됨
            } else {
                // 필요한 권한 중 일부 또는 모두 거부됨
            }
        }
    }
}

