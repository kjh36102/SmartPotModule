package com.example.app;

import static com.example.app.Fragment1.score;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.tabs.TabLayout;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;


import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment2 newInstance} factory method to
 * create an instance of this fragment.
 */



public class Fragment2 extends Fragment {
    //Explan Explan;
    public static HashMap<String, String> mDataHashMap;
    private static final String IS_FIRST_TIME = "IsFirstTime";
    private static final String NAME_KEY = "name_key";
    private static final String TIPS = "tips";
    private static final String FEEDBACK = "feedbacks";
    private static final String PREFERENCE_NAME = "MyPreference";
    String explan,tips;
    String shortExplan,shortTips; // 짧은 설명
    String textExplan = explan;  //현재 화면에 나와야할 설명
    String tTips =  tips;

    String dateTime;
    public static String temp; //현재 온도,습도,조도,산화도,n,p,k,전기 전도도 값
    public static String humid;
    public static String ph;
    public static String light;
    public static String phos;
    public static String pota;
    public static String nitro;
    public static String ec;

    String name = "선인장"; //현재 이름
//  String name = popup.plant;




    public boolean null_judge() {
       /* TextView trash = (TextView)getView().findViewById(R.id.trash);
        trash.setVisibility(View.GONE);*/
        int i = 0;
        if (temp == null||temp == "0") {
            temp = "0";
            i++;
        }
        if (humid == null||humid == "0") {
            humid = "0";
            i++;
        }
        if (light == null||light == "0") {
            light = "0";
            i++;
        }
        if (nitro == null||nitro == "0") {
            nitro = "0";
            i++;
        }
        if (ph == null||ph == "0") {
            ph = "0";
            i++;
        }
        if (phos == null||phos== "0") {
            phos = "0";
            i++;
        }
        if (pota == null||pota == "0") {
            pota = "0";
            i++;
        }
        if (ec == null||ec == "0") {
            ec = "0";
            i++;
        }
        if (i == 8) {

            //  trash.setText(""+i);
            return true;
        }
        else{
            //     trash.setText(""+i);
            return false;
        }



    }//혹시 식물정보가 불려지지 않았을경우 0으로 표기


    public void getShortExplan(Button btn){
        tTips = tips;
        if(explan.length()>95){
            shortExplan = explan.substring(0,92)+"..."; // 설명으 세줄로 자르고 뒤에 ...으로 설명을 덧붙임
            btn.setVisibility(View.VISIBLE);//더보기 버튼 등장
            textExplan = shortExplan;//현재 설명에 짧은 설명으로 적용
        }
        else{
            shortExplan = null;
            btn.setVisibility(View.GONE);//더보기 버튼 삭제
        }//설명이 짧을 경우 짧은 설명은 NULL

    }//짧은 설명 판단 및 만드는 함수
    public void getShortTips(Button btn){
        tTips = tips;
        if(tips.length()>95){
            shortTips = tips.substring(0,92)+"..."; // 설명으 세줄로 자르고 뒤에 ...으로 설명을 덧붙임
            btn.setVisibility(View.VISIBLE);//더보기 버튼 등장
            tTips = shortTips;//현재 설명에 짧은 설명으로 적용
        }
        else{
            shortTips = null;
            btn.setVisibility(View.GONE);//더보기 버튼 삭제
        }//설명이 짧을 경우 짧은 설명은 NULL

    }//짧은 설명 판단 및 만드는 함수

    /*private class GetJsonDataTask extends AsyncTask<String, Void, HashMap<String, String>> {
        @Override
        protected HashMap<String, String> doInBackground(String... urls) {
            mDataHashMap = null;
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
                resultHashMap.put("temp", jsonObject.getString("tm"));
                resultHashMap.put("humid", jsonObject.getString("hm"));
                resultHashMap.put("light", jsonObject.getString("lt"));
                resultHashMap.put("ph", jsonObject.getString("ph"));
                resultHashMap.put("nitro", jsonObject.getString("n"));
                resultHashMap.put("phos", jsonObject.getString("p"));
                resultHashMap.put("pota", jsonObject.getString("k"));
                resultHashMap.put("ec", jsonObject.getString("ec"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return resultHashMap;
        }

        @Override
        public void onPostExecute(HashMap<String, String> resultHashMap) {
            mDataHashMap = resultHashMap;
            try {
                updateDataTextView();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }



    }


    private void updateDataTextView() throws InterruptedException {
        if (mDataHashMap != null) {
            temp = mDataHashMap.get("temp");
            humid = mDataHashMap.get("humid");
            light = mDataHashMap.get("light");
            ph = mDataHashMap.get("ph");
            nitro = mDataHashMap.get("nitro");
            phos = mDataHashMap.get("phos");
            pota = mDataHashMap.get("pota");
            ec = mDataHashMap.get("ec");
            TextView trash1 = getView().findViewById(R.id.trash);

            TextView rTxt = getView().findViewById(R.id.rTxt);

            TextView textView1 = getView().findViewById(R.id.explan_text);
            ImageButton btn3 = (ImageButton) getView().findViewById(R.id.update);


            if (null_judge() == true) {

                textView1.setText("오류입니다 새로고침을 눌러주세요");



            }

            else {
                gpt_feedback(getView(),textView1, Double.parseDouble(temp), Double.parseDouble(humid), Double.parseDouble(light), Double.parseDouble(ph), Double.parseDouble(nitro), Double.parseDouble(phos), Double.parseDouble(pota), Double.parseDouble(ec));
                //trash1.setText(""+temp+"℃, 토양 습도는 "+humid+"%, 질소가 "+nitro+"mg/kg, 인이 "+phos+"mg/kg, 칼륨이 "+pota+" mg/kg, 토양 ph가 "+ph+" 전기 전도도는 "+ec+"μS/cm, 광량은 "+light+"lux이다");

            }

            btn3.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("마지막 업데이트 시간 : yyyy-MM-dd_HH:mm", Locale.CANADA);
            String dateTime = dateFormat.format(calendar.getTime());

            rTxt.setText(dateTime);
        }
    }*/

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
                resultHashMap.put("temp", jsonObject.getString("tm"));
                resultHashMap.put("humid", jsonObject.getString("hm"));
                resultHashMap.put("light", jsonObject.getString("lt"));
                resultHashMap.put("ph", jsonObject.getString("ph"));
                resultHashMap.put("nitro", jsonObject.getString("n"));
                resultHashMap.put("phos", jsonObject.getString("p"));
                resultHashMap.put("pota", jsonObject.getString("k"));
                resultHashMap.put("ec", jsonObject.getString("ec"));
                //resultHashMap.put("ts", jsonObject.getString("ts"));
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
            try {
                updateDataTextView();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateDataTextView() throws InterruptedException {
        if (mDataHashMap != null) {
            temp = mDataHashMap.get("temp");
            humid = mDataHashMap.get("humid");
            light = mDataHashMap.get("light");
            ph = mDataHashMap.get("ph");
            nitro = mDataHashMap.get("nitro");
            phos = mDataHashMap.get("phos");
            pota = mDataHashMap.get("pota");
            ec = mDataHashMap.get("ec");
            String ts = mDataHashMap.get("ts");

            TextView rTxt = getView().findViewById(R.id.rTxt);
            TextView textView1 = getView().findViewById(R.id.explan_text);

            TextView trash1 = getView().findViewById(R.id.trash);

            ImageButton update_btn = (ImageButton) getView().findViewById(R.id.update);
            Button btn = (Button)getView().findViewById(R.id.more_2);
            Button btn2 = (Button)getView().findViewById(R.id.close_2);


            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat=new SimpleDateFormat("마지막 업데이트 시간 : yyyy-MM-dd_HH:mm");
            dateTime = dateFormat.format(calendar.getTime());

            SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();

            editor.putString("datetime", dateTime);
            editor.apply();



            rTxt.setText(dateTime);
            ChatGPT chatGPT = new ChatGPT();

            if (null_judge() == true) {

                textView1.setText("오류입니다 새로고침을 눌러주세요");



            }

            else {
                Thread thread = new Thread() {
                    public void run() {

                        explan = chatGPT.feedback(name, Double.parseDouble(temp), Double.parseDouble(humid), Double.parseDouble(nitro), Double.parseDouble(phos), Double.parseDouble(pota), Double.parseDouble(ph), Double.parseDouble(ec), Double.parseDouble(light));

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                                editor.putString(FEEDBACK, explan);
                                editor.apply();


                                btn2.setVisibility(View.GONE);//닫기 버튼 비활성화

                                getShortExplan(btn);// 설명 길이 판단및 짧은 설명 제작
                                textView1.setText(textExplan);// 최종 현재 설명 적용

                                if (btn.getVisibility() == View.VISIBLE) {
                                    btn.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View view) {
                                            btn2.setVisibility(View.VISIBLE);
                                            btn.setVisibility(View.GONE);
                                            textView1.setText(explan);

                                        }

                                    });
                                }//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화


                                btn2.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View view) {
                                        btn.setVisibility(View.VISIBLE);
                                        btn2.setVisibility(View.GONE);
                                        textView1.setText(shortExplan);

                                    }

                                });//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화
                            }
                        });
                    }
                };
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }


            Fragment3.humid = humid;
            Fragment3.light = light;


        }
    }


    @SuppressLint("SuspiciousIndentation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_2, container, false);

        TextView textView1 = (TextView) view.findViewById(R.id.explan_text);//설명 text 적용
        TextView textView2 = (TextView) view.findViewById(R.id.Tips);
        TextView textName = (TextView) view.findViewById(R.id.textView) ;//식물 이름
        TextView rTxt = (TextView)view.findViewById(R.id.rTxt);
        textName.setText(name);//식물이름 적용



        Button btn = (Button) view.findViewById(R.id.more_2);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.close_2);//닫기 버튼
        Button btn3 = (Button) view.findViewById(R.id.more);//더보기 버튼
        Button btn4 = (Button) view.findViewById(R.id.close);//닫기 버튼
        ImageButton update_btn = (ImageButton)view.findViewById(R.id.update);

        btn.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);
        btn4.setVisibility(View.GONE);
        update_btn.setVisibility(View.VISIBLE);


        TextView trash1 = view.findViewById(R.id.trash);
        TextView trash2 = view.findViewById(R.id.trash2);
        TextView trash3 = view.findViewById(R.id.trash3);
        Button button = view.findViewById(R.id.t_button);

        trash1.setVisibility(View.GONE);
        trash2.setVisibility(View.GONE);
        trash3.setVisibility(View.GONE);
        button.setVisibility(View.GONE);

        ChatGPT chatGPT = new ChatGPT();



        if(!MainActivity.sharedPreferences_fragment2.contains(FEEDBACK)){

            new GetJsonDataTask().execute(popup.url);
            textView1.setText("...로딩중...");
            btn.setVisibility(View.GONE);
            btn2.setVisibility(View.GONE);




        }
        else{
            rTxt.setText(MainActivity.sharedPreferences_fragment2.getString("datetime",""));
            explan = MainActivity.sharedPreferences_fragment2.getString(FEEDBACK,"");
            getShortExplan(btn);
            textView1.setText(textExplan);// 최종 현재 설명 적용
            if (btn.getVisibility() == View.VISIBLE) {
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        btn2.setVisibility(View.VISIBLE);
                        btn.setVisibility(View.GONE);
                        textView1.setText(explan);

                    }

                });
            }//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화


            btn2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    btn.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.GONE);
                    textView1.setText(shortExplan);

                }

            });//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화




        }




        if(!MainActivity.sharedPreferences_fragment2.contains(NAME_KEY)) {
            textView2.setText("...로딩중...");
            gpt_tips(view);
            SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
            editor.putString(NAME_KEY, name);
            editor.apply();




        }
        else if(!MainActivity.sharedPreferences_fragment2.getString(NAME_KEY,"").equals(name)){
            textView2.setText("...로딩중...");
            gpt_tips(view);
            SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
            editor.putString(NAME_KEY, name);
            editor.apply();

            new GetJsonDataTask().execute(popup.url);
            textView1.setText("...로딩중...");
            btn.setVisibility(View.GONE);
            btn2.setVisibility(View.GONE);



        }
        else if(MainActivity.sharedPreferences_fragment2.getString(NAME_KEY,"").equals(name)){
            tips = MainActivity.sharedPreferences_fragment2.getString(TIPS,"");
            getShortTips(btn3);
            textView2.setText(tTips);
            if (btn3.getVisibility() == View.VISIBLE) {
                btn3.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        btn4.setVisibility(View.VISIBLE);
                        btn3.setVisibility(View.GONE);
                        textView2.setText(tips);

                    }

                });
            }//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화


            btn4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    btn3.setVisibility(View.VISIBLE);
                    btn4.setVisibility(View.GONE);
                    textView2.setText(tTips);

                }

            });//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화

        }


        View finalView = view;
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new GetJsonDataTask().execute(popup.url);
                textView1.setText("...로딩중...");
                btn.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);



            }
        });//새로고침 버튼을 클릭하면 새 더미데이터 가져옴






        return view;
    }



    public void gpt_tips(View view) {
        ChatGPT chatGPT = new ChatGPT();
        Button btn = (Button) view.findViewById(R.id.more);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.close);//닫기 버튼
        TextView textView1 = (TextView) view.findViewById(R.id.Tips);

        new Thread() {
            public void run() {

                tips = chatGPT.tips(name);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                        editor.putString(TIPS, tips);
                        editor.apply();

                        btn2.setVisibility(View.GONE);//닫기 버튼 비활성화

                        getShortTips(btn);// 설명 길이 판단및 짧은 설명 제작
                        textView1.setText(tTips);// 최종 현재 설명 적용

                        if (btn.getVisibility() == View.VISIBLE) {
                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    btn2.setVisibility(View.VISIBLE);
                                    btn.setVisibility(View.GONE);
                                    textView1.setText(tips);

                                }

                            });
                        }//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화


                        btn2.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                btn.setVisibility(View.VISIBLE);
                                btn2.setVisibility(View.GONE);
                                textView1.setText(shortTips);

                            }

                        });//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화
                    }
                });
            }

        }.start();
    }//chat gpt 적용하여 피드백 가져오기






    public void gpt_feedback(View view, TextView textView1, Double temp, Double humid, Double light, Double ph, Double nitro, Double phos, Double pota, Double ec) throws InterruptedException {
        ChatGPT chatgpt_1 = new ChatGPT();
        Button btn = (Button) view.findViewById(R.id.more_2);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.close_2);//닫기 버튼
        ImageButton update_btn = (ImageButton) view.findViewById(R.id.update);



        Thread thread = new Thread() {
            public void run() {
                update_btn.setVisibility(View.GONE);
                explan = chatgpt_1.feedback(name, temp, humid, nitro, phos, pota, ph, ec, light);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                        editor.putString(FEEDBACK, explan);
                        editor.apply();


                        btn2.setVisibility(View.GONE);//닫기 버튼 비활성화

                        getShortExplan(btn);// 설명 길이 판단및 짧은 설명 제작
                        textView1.setText(textExplan);// 최종 현재 설명 적용

                        if (btn.getVisibility() == View.VISIBLE) {
                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    btn2.setVisibility(View.VISIBLE);
                                    btn.setVisibility(View.GONE);
                                    textView1.setText(explan);

                                }

                            });
                        }//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화


                        btn2.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                btn.setVisibility(View.VISIBLE);
                                btn2.setVisibility(View.GONE);
                                textView1.setText(shortExplan);

                            }

                        });//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화
                    }
                });
            };

        };

        thread.start();
        thread.join();
        update_btn.setVisibility(View.VISIBLE);


    }//chat gpt 적용하여 피드백 가져오기
    //그래프 레이아웃을 가져와 해시맵에 저장



}