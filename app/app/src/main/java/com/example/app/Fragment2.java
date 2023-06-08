package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
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

    String explan;
    String shortExplan; // 짧은 설명
    String textExplan = explan;  //현재 화면에 나와야할 설명

    String temp,humid,light,ph,phos,pota,nitro,ec; //현재 온도,습도,조도,산화도,n,p,k,전기 전도도 값

    String name = "선인장"; //현재 이름


    BarChart bar_chart[] = new BarChart[8]; // 그래프들
    HashMap<String,BarChart> allBarChart; // 그래프들의 hashmap
    HashMap<String, Float> value = new HashMap<>();// 현재 식물의 상태값들



    public boolean null_judge() {
        int i = 0;
        if (temp == null) {
            temp = "0";
            i++;
        }
        if (humid == null) {
            humid = "0";
            i++;
        }
        if (light == null) {
            light = "0";
            i++;
        }
        if (nitro == null) {
            nitro = "0";
            i++;
        }
        if (ph == null) {
            ph = "0";
            i++;
        }
        if (phos == null) {
            phos = "0";
            i++;
        }
        if (pota == null) {
            pota = "0";
            i++;
        }
        if (ec == null) {
            ec = "0";
            i++;
        }
        if (i == 8) return true;
        else return false;

    }//혹시 식물정보가 불려지지 않았을경우 0으로 표기
    public void reset(){

        value = new HashMap<>();
        null_judge();
        value.put("temp",Float.parseFloat(temp));
        value.put("humid",Float.parseFloat(humid));
        value.put("light",Float.parseFloat(light));
        value.put("ph",Float.parseFloat(ph));
        value.put("nitro",Float.parseFloat(nitro));
        value.put("phos",Float.parseFloat(phos));
        value.put("pota",Float.parseFloat(pota));
        value.put("ec",Float.parseFloat(ec));


    }//식물 값들 초기화

    private SwipeRefreshLayout mysrl; //당겨서 새로 고침의 레이아웃

    public void getShortExplan(Button btn){
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
    private class GetJsonDataTask extends AsyncTask<String, Void, HashMap<String, String>> {
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
                //resultHashMap.put("ts", jsonObject.getString("ts"));
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
            updateDataTextView();
        }

        public String getTemp() {
            return temp;
        }

        public String getHumid() {
            return humid;
        }

        public String getLight() {
            return light;
        }

        public String getNitro() {
            return nitro;
        }

        public String getPhos() {
            return phos;
        }

        public String getPota() {
            return pota;
        }

        public String getPh() {
            return ph;
        }

        public String getEc() {
            return ec;
        }


    }

    private void updateDataTextView() {
        if (mDataHashMap != null) {
            temp = mDataHashMap.get("temp");
            humid = mDataHashMap.get("humid");
            light = mDataHashMap.get("light");
            ph = mDataHashMap.get("ph");
            nitro = mDataHashMap.get("nitro");
            phos = mDataHashMap.get("phos");
            pota = mDataHashMap.get("pota");
            ec = mDataHashMap.get("ec");
            TextView tempText = getView().findViewById(R.id.temp);
            TextView humidText = getView().findViewById(R.id.humid);
            TextView lightText = getView().findViewById(R.id.light);
            TextView phText = getView().findViewById(R.id.ph);
            TextView nitroText = getView().findViewById(R.id.nitro);
            TextView phosText = getView().findViewById(R.id.phos);
            TextView potaText = getView().findViewById(R.id.pota);
            TextView ecText = getView().findViewById(R.id.ec);
            TextView rTxt = getView().findViewById(R.id.rTxt);
            if (null_judge() == true) {
                TextView textview = (TextView) getView().findViewById(R.id.explan_text);
                textview.setText("오류입니다 새로고침을 눌러주세요");
            } else {
                gpt_feedback(getView(), Float.parseFloat(temp), Float.parseFloat(humid), Float.parseFloat(light), Float.parseFloat(ph), Float.parseFloat(nitro), Float.parseFloat(phos), Float.parseFloat(pota), Float.parseFloat(ec));
            }
            initChart(getView(), allBarChart.get("temp"), "온도", 21, Float.parseFloat(temp));
            initChart(getView(), allBarChart.get("humid"), "습도", 12.5f, Float.parseFloat(humid));
            initChart(getView(), allBarChart.get("light"), "조도", 10000, Float.parseFloat(light));
            initChart(getView(), allBarChart.get("ph"), "PH", 6.25F, Float.parseFloat(ph));
            initChart(getView(), allBarChart.get("nitro"), "N", 11.5f, Float.parseFloat(nitro));
            initChart(getView(), allBarChart.get("phos"), "P", 5.5f, Float.parseFloat(phos));
            initChart(getView(), allBarChart.get("pota"), "k", 11.5f, Float.parseFloat(pota));
            initChart(getView(), allBarChart.get("ec"), "EC", 3, Float.parseFloat(ec));
            tempText.setText(temp);
            humidText.setText(humid);
            lightText.setText(light);
            phText.setText(ph);
            nitroText.setText(nitro);
            phosText.setText(phos);
            potaText.setText(pota);
            ecText.setText(ec);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("마지막 업데이트 시간 : yyyy-MM-dd_HH:mm", Locale.CANADA);
            String dateTime = dateFormat.format(calendar.getTime());

            rTxt.setText(dateTime);
        }
    }
    @SuppressLint("SuspiciousIndentation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_2, container, false);

        new GetJsonDataTask().execute(popup.url);// 처음에 더미데이터 불러오기
        reset();//Value 초기화
        //gpt_standard(view);

        /*TextView trash = (TextView) view.findViewById(R.id.trash);
        trash.setText("");*/
       /* if(value == null){
            trash.setText("없음");
        }
        else{
            String t = ""+value.get("temp");
            trash.setText(t);
        }*/
        TextView trash1 = view.findViewById(R.id.trash);
        TextView trash2 = view.findViewById(R.id.trash2);
        TextView trash3 = view.findViewById(R.id.trash3);
        Button button = view.findViewById(R.id.t_button);
        trash1.setVisibility(View.GONE);
        ChatGPT chatgpt = new ChatGPT();

        //gpt_standard(getView());


        trash2.setVisibility(View.GONE);
        trash3.setVisibility(View.GONE);
        button.setVisibility(View.GONE);



        TextView textView1 = (TextView) view.findViewById(R.id.explan_text);//설명 text 적용
        Button btn = (Button) view.findViewById(R.id.button);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.button2);//닫기 버튼
        btn.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);

        String explan_1;



        LinearLayout graph_1 = (LinearLayout) view.findViewById(R.id.graph_layout_1);//그래프 첫째줄 레이아웃
        LinearLayout graph_2 = (LinearLayout) view.findViewById(R.id.graph_layout_2);//그래프 둘째줄 레이아웃
        LinearLayout graph_3 = (LinearLayout) view.findViewById(R.id.graph_layout_3);//그래프 셋째줄 레이아웃
        LinearLayout table_1 = (LinearLayout) view.findViewById(R.id.table_layout1);//표 첫째줄 레이아웃
        LinearLayout table_2 = (LinearLayout) view.findViewById(R.id.table_layout2);//표 둘째줄 레이아웃

        //첫 화면은 그래프를 띄워주게함, 그래프 레이아웃을 제외한 표 레이아웃은 보이지 않게 적용
        graph_1.setVisibility(View.VISIBLE);
        graph_2.setVisibility(View.VISIBLE);
        graph_3.setVisibility(View.VISIBLE);
        table_1.setVisibility(View.GONE);
        table_2.setVisibility(View.GONE);

        TabLayout tabs = (TabLayout)view.findViewById(R.id.tab_mode) ;
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Fragment selected = null;
                if (position == 0) {//첫번째 탭이 선택되면 그래프만 보여줌
                    graph_1.setVisibility(View.VISIBLE);
                    graph_2.setVisibility(View.VISIBLE);
                    graph_3.setVisibility(View.VISIBLE);
                    table_1.setVisibility(View.GONE);
                    table_2.setVisibility(View.GONE);
                } else if (position == 1) {//두번째 탭이 선택되면 표만 보여줌
                    graph_1.setVisibility(View.GONE);
                    graph_2.setVisibility(View.GONE);
                    graph_3.setVisibility(View.GONE);
                    table_1.setVisibility(View.VISIBLE);
                    table_2.setVisibility(View.VISIBLE);
                }
            }
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });// 탭 레이아웃



        Button btn_1 = view.findViewById(R.id.update);


        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetJsonDataTask().execute(popup.url);//새 더미데이터 가져오기
                //String t1 = ""+value.get("temp");
                textView1.setText("...로딩중...");
                btn.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);


            }
        });//새로고침 버튼을 클릭하면 새 더미데이터 가져옴






        TextView textName = (TextView) view.findViewById(R.id.textView) ;//식물 이름
        textName.setText(name);//식물이름 적용






        mysrl = view.findViewById(R.id.content_srl);//당겨서 새로고침 레이아웃 적용

        View finalView = view;
        //new GetJsonDataTask().execute(MainActivity.URL);






        mysrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetJsonDataTask().execute(popup.url);//새 더미데이터 가지오기
                textView1.setText("...로딩중...");
                btn.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                //null_judge();


                //그래프 그리기


                mysrl.setRefreshing(false);
            }//당겨서 새로고치면 더미데이터를 가져와 다시 그래프를 그리고, 표도 다시 그림
        });




        hashMap(view);// 그래프 레이아웃들을 그래프 해시맵에 저장
        //reset();

        initChart(finalView,allBarChart.get("temp"),"온도",21,23);
        initChart(finalView,allBarChart.get("humid"),"습도",12.5f,20);
        initChart(finalView,allBarChart.get("light"),"조도",10000,10000);
        initChart(finalView,allBarChart.get("ph"),"PH",6.25F,5);
        initChart(finalView,allBarChart.get("nitro"),"N",11.5f,10);
        initChart(finalView,allBarChart.get("phos"),"P",5.5f,5);
        initChart(finalView,allBarChart.get("pota"),"k",11.5f,11);
        initChart(finalView,allBarChart.get("ec"),"EC",3,3);
        //그래프 그리기

        /*if(value != null){
            initChart(view, allBarChart.get("temp"), "온도", 24, value.get("temp"));
            initChart(view, allBarChart.get("humid"), "습도", 25, 23);
            initChart(view, allBarChart.get("light"), "조도", 10000, 23);
            initChart(view, allBarChart.get("ph"), "PH", 7, 23);
            initChart(view, allBarChart.get("nitro"), "N", 10, 23);
            initChart(view, allBarChart.get("phos"), "P", 6, 23);
            initChart(view, allBarChart.get("pota"), "k", 10, 23);
            initChart(view, allBarChart.get("ec"), "EC", 15, 23);

        }*/

        return view;
    }



    public void gpt_standard(View view){
        ChatGPT chatGPT = new ChatGPT();
        TextView trash = (TextView)view.findViewById(R.id.trash);


        new Thread() {
            public void run() {

                String t = chatGPT.recommand(name);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        trash.setText("");
                    }
                });
            }

        }.start();
    }

    public void gpt_feedback(View view, float temp, float humid, float light, float ph, float nitro, float phos, float pota, float ec) {
        ChatGPT chatgpt_1 = new ChatGPT();
        Button btn = (Button) view.findViewById(R.id.button);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.button2);//닫기 버튼
        TextView textView1 = (TextView) view.findViewById(R.id.explan_text);
        new Thread() {
            public void run() {

                explan = chatgpt_1.feedback(name, temp, humid, nitro, phos, pota, ph, ec, light);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

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

        }.start();
    }//chat gpt 적용하여 피드백 가져오기
    public void hashMap(View chartView){
        allBarChart = new HashMap<>();

        bar_chart[0] = chartView.findViewById(R.id.chart_temp);
        bar_chart[1] = chartView.findViewById(R.id.chart_humid);
        bar_chart[2] = chartView.findViewById(R.id.chart_light);
        bar_chart[3] = chartView.findViewById(R.id.chart_ph);
        bar_chart[4] = chartView.findViewById(R.id.chart_nitro);
        bar_chart[5] = chartView.findViewById(R.id.chart_phos);
        bar_chart[6] = chartView.findViewById(R.id.chart_pota);
        bar_chart[7] = chartView.findViewById(R.id.chart_ec);

        allBarChart.put("temp",bar_chart[0]);
        allBarChart.put("humid",bar_chart[1]);
        allBarChart.put("light",bar_chart[2]);
        allBarChart.put("ph",bar_chart[3]);
        allBarChart.put("nitro",bar_chart[4]);
        allBarChart.put("phos",bar_chart[5]);
        allBarChart.put("pota",bar_chart[6]);
        allBarChart.put("ec",bar_chart[7]);

    }//그래프 레이아웃을 가져와 해시맵에 저장
    private BarChart initChart(View chartView,BarChart bb,String now,float s_v,float v){ //그래프 그리는 함수 ,, 그래프를 가져와 now는 값의 이름, 온도또는 습도등
        BarChart barChart = bb;                                                         //s_v는 추천값의 중간값, v는 현재 상태

       // barChart = chartView.findViewById(R.id.chart_temp);
        barChart.setDrawValueAboveBar(true);

        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();//그래프 상단 x축 설정
        xAxis.setEnabled(false);//비활성화


        YAxis leftAxis = barChart.getAxisLeft();//왼쪽 y축 설정
        leftAxis .setAxisMinimum(0.0f);//가장 작은값 0 설정
        if(v>s_v*2){
            leftAxis.setAxisMaxValue(v+2);//그래프 현재값이 추천값보다 두배이상 크면 현재값의 높이에 맞게 그래프 높이조절
        }
        else{
            leftAxis.setAxisMaxValue(s_v*2);//그래프 현재값이 추천값보다 두배이상 작을 경우 추천값의 두배 높이의 그래프 설정
        }
        YAxis rightAxis = barChart.getAxisRight();//오른쪽 y축 설정
        rightAxis.setEnabled(false);//비활성화

        Legend legend = barChart.getLegend();//그래프별 이름및 아이콘 설정
        legend.setTextColor(Color.GRAY);//그래프 이름은 회색
        legend.setTextSize(5.0f);//그래프 이름 크기 설정
        legend.setXEntrySpace(3.0f);// 그래프 이름 사이 공간 설정
        legend.setFormToTextSpace(1.0f);// 그래프 이름과 아이콘 사이 공간 설정


        barChart.animateXY(1500,1500);// 그래프 나타나는 애니메이션 속도 설정

        ArrayList<BarEntry> entries = new ArrayList<>(); //그래프에 들어갈 추천값
        ArrayList<BarEntry> entries_2 = new ArrayList<>();//그래프에 들어갈 현재값
        entries.add(new BarEntry(1.0f, s_v)); //x좌표 1에 추천값만큼의 높이를 가진 막대그래프 설정
        entries_2.add(new BarEntry(2.0f, v));//x좌표 2에 현재값만큼의 높이를 가진 막대그래프 설정


        BarDataSet dataSet2 = new BarDataSet(entries, "최적 "+now); // 그래프 별 이름 설정
        BarDataSet dataSet = new BarDataSet(entries_2, "현재 "+now);//
        dataSet2.setColor(Color.rgb(240, 120, 124));//그래프 색상 설정
        dataSet.setColor(Color.rgb(120,120,180));





        BarData data = new BarData(dataSet2,dataSet); // 그래프 데이터 셋 적용
        data.setValueTextSize(10f); // 그래프의 수치 크기 설정
        data.setDrawValues(false);//
        data.setBarWidth(0.4f);


        barChart.setTouchEnabled(false);//그래프 터치 불가능하게 해놓음
        barChart.setData(data);//그래프 데이터 삽입
        barChart.invalidate();//차트 갱신
        return barChart;
    }
}