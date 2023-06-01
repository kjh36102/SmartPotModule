package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    TabPagerAdapter adapter;
    String[] tabName = new String[]{"대시보드", "상세분석", "식물관리"};
    public static final String URL = "http://cofon.xyz:9090/read?col=temp_humid_light_ph_nitro_phos_pota_ec";
    //습도(humid), 온도(temp), 전기전도도(ec), 산화도(ph), 질소(nitro), 인(phos), 칼륨(pota), 광량(light);
    public static HashMap<String, String> mDataHashMap;
<<<<<<< Updated upstream

=======
    private static final int PERMISSION_REQUEST_CODE = 0;

    public static SharedPreferences sharedPreferences_fragment2;
>>>>>>> Stashed changes
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences_fragment2 = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);


        toolbar = (Toolbar) findViewById(R.id.toolbar);           // actionbar에서 toolbar로 변경
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle("SmartPotModule");

        //xml 연결
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.pager);
        //adapter 준비 및 연결
        adapter = new TabPagerAdapter(this);
        viewPager.setAdapter(adapter);
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
        //JSON서버 연결
        new GetJsonDataTask().execute(URL);
        //조명상태 불러오는 코드 추가해야함
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
                resultHashMap.put("temp", jsonObject.getString("temp"));
                resultHashMap.put("humid", jsonObject.getString("humid"));
                resultHashMap.put("light", jsonObject.getString("light"));
                resultHashMap.put("ph", jsonObject.getString("ph"));
                resultHashMap.put("nitro", jsonObject.getString("nitro"));
                resultHashMap.put("phos", jsonObject.getString("phos"));
                resultHashMap.put("pota", jsonObject.getString("pota"));
                resultHashMap.put("ec", jsonObject.getString("ec"));
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
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat=new SimpleDateFormat("마지막 업데이트 시간 : yyyy-MM-dd_HH:mm");
            String dateTime = dateFormat.format(calendar.getTime());
            rTxt.setText(dateTime);
        }
    }
}
