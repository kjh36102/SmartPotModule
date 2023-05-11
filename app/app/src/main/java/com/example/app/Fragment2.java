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

class Chat_gpt {
    static URL url;
    HttpURLConnection con;
    static org.json.simple.JSONObject input;
    static String key;
    public Chat_gpt(){
        try {
            url = new URL("https://api.openai.com/v1/completions");
            input = new org.json.simple.JSONObject();
            input.put("model", "text-davinci-003");
            input.put("prompt", "");
            input.put("max_tokens", 2048);
            this.key = "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";
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String feedback(String name, double temperature, double humidity, double nitrogen, double phosphorus, double potassium, double ph, double ec, double lux) {
        this.input.put("prompt", "식물 "+name+"의 토양 온도는 "+temperature+"도, 토양 습도는 "+humidity+"%, 질소가 "+nitrogen+"mg/kg, 인이 "+phosphorus+"mg/kg, 칼륨이 "+potassium+" mg/kg, 토양 ph가 "+ph+" 전기 전도도는 "+ec+"us/cm, 광량은 "+lux+"lux이다. 문제가 있는 것을 분석해주고, 개선방안을 제안해줘.");
        return process();
    }
    public String recommand(String name){
        this.input.put("prompt", "식물 "+name+"의 추천 토양 온도(섭씨), 추천 토양 습도(%), 추천 N(mg/kg), 추천 P(mg/kg), 추천 K(mg/kg), 추천 토양산화도(ph), 추천 토양전기전도도(us/cm), 추천 광량(lux)의 각 수치를 범위로 만들어서 아래처럼 json을 만들어줘\n" +
                "{\"추천_토양온도\": {\"최소값\": xx,\"최대값\": xx,\"단위\": \"℃\"}, \"추천_토양습도\": {...}, \"추천_N\": {...}, \"추천_P\": {...}, \"추천_K\": {...}, \"추천_토양산화도\": {...}, \"추천_토양전기전도도\": {...},  \"추천_광량\": {...}}");
        return process();
    }
    public static String process(){
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Authorization", "Bearer " + key);
            con.setDoOutput(true);
            System.out.println(input.toString());
            // 연결의 출력 스트림에 입력 데이터 쓰기

            try (OutputStream os = con.getOutputStream()) {
                String jsonRequest = input.toString();
                byte[] inputBytes = jsonRequest.getBytes("utf-8");
                os.write(inputBytes, 0, inputBytes.length);
            }

            // API 서버에서 응답 코드 가져오기

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                return "error";
            } else {
                System.out.println("Response Code : " + responseCode);
            }

            // API 서버에서 응답을 읽습니다.
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    //공백제거
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                //String 데이터를 Object로 변환
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response.toString());
                // Object 데이터를 JSONObject로 변환
                org.json.simple.JSONObject jsonResponse = (org.json.simple.JSONObject) obj;
                //jsonResponse에서 text 부분만 추출하기 위한 과정
                JSONArray choices = (JSONArray) jsonResponse.get("choices");
                // choices 부분 추출하여 [] 배열 벗기기
                org.json.simple.JSONObject choice = (org.json.simple.JSONObject) choices.get(0);
                // text 필드 추출
                String text = (String) choice.get("text");
                con.disconnect();
                return text;
            }
        } catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }
}


public class Fragment2 extends Fragment {
    //Explan Explan;
    public static HashMap<String, String> mDataHashMap;

    String explan = "주어진 정보를 바탕으로 선인장 식물의 토양 환경에 대한 문제점을 분석하고 개선 방안을 제안해 보겠습니다.\n" +
            "\n" +
            "토양 온도: 23도 - 토양 온도는 일반적으로 섭씨 18도에서 24도 사이인 선인장 식물에 적합한 범위 내에 있습니다. 따라서 토양 온도는 적절한 범위 내에 있으며 문제가 되지 않습니다.\n" +
            "\n" +
            "토양 습도: 20% - 선인장은 사막 지역이 원산지이기 때문에 건조한 조건을 선호합니다. 따라서, 낮은 토양 습도가 문제가 될 수 있습니다. 선인장을 재배할 때는 토양 습도를 10%에서 15% 사이로 유지하는 것이 좋습니다. 이 문제를 해결하려면 흙에 물을 너무 자주 주지 않도록 하거나 냄비에 구멍을 내거나 배수가 잘 되는 토양 혼합물을 사용하여 적절한 배수를 보장해야 합니다.\n" +
            "\n" +
            "질소: 10mg/kg - 질소는 식물 성장에 중요한 영양소입니다. 선인장은 일반적으로 더 낮은 질소 수준을 필요로 하지만, 10mg/kg은 상대적으로 낮습니다. 질소 결핍은 잎이 노랗게 변하거나 성장이 둔화되는 것으로 나타날 수 있습니다. 질소를 보충하기 위해 액체 질소 비료를 사용하거나 질소 함량이 높은 비료를 추가할 수 있습니다. 그러나 지침을 따르고 과도한 수정을 피하는 것이 중요합니다.\n" +
            "\n" +
            "인 및 칼륨: 인은 5mg/kg, 칼륨은 11mg/kg입니다. 인과 칼륨은 식물의 성장과 개화에 필수적인 영양소입니다. 주어진 인과 칼륨 수치는 적절한 범위 내에 있는 것으로 보입니다. 선인장은 상대적으로 낮은 수준의 인과 칼륨으로 번성할 수 있지만, 비료에 이러한 영양소를 포함하면 식물의 전반적인 건강을 유지하는 데 도움이 될 수 있습니다.\n" +
            "\n" +
            "토양 pH: 5 - 선인장 식물은 일반적으로 약간 산성의 토양을 선호합니다. 주어진 pH 값 5는 선인장에 적합한 범위 내에 있습니다. 따라서 토양 pH는 문제가 되지 않는 것 같습니다.\n" +
            "\n" +
            "분석을 요약하자면:\n" +
            "\n" +
            "토양 온도는 선인장 식물에 적합합니다.\n" +
            "토양 습도는 낮으며 10-15% 정도로 조절해야 합니다.\n" +
            "질소 수준은 상대적으로 낮으며 질소 보충이 필요할 수 있습니다.\n" +
            "인과 칼륨 수치는 적절한 범위 내에 있습니다.\n" +
            "토양 pH는 선인장 식물에 적합합니다.\n" +
            "개선 계획:\n" +
            "\n" +
            "토양 습도 조절: 흙에 물을 자주 주지 않고 냄비에 구멍을 내거나 배수가 잘 되는 흙 혼합물을 사용하여 적절한 배수를 보장합니다.\n" +
            "질소 보충: 식물에 필요한 영양분을 공급하기 위해 질소가 풍부한 비료 또는 액체 질소 비료를 사용합니다.\n" +
            "인 및 칼륨 수준 유지: 식물의 성장과 개화를 지원하기 위해 영양소 혼합물에 인과 칼륨이 포함된 비료를 포함합니다.\n" +
            "발전소 반응 모니터링: 식물의 전반적인 상태, 성장 및 색상을 주시하여 식물의 건강 상태에 대한 조정이 긍정적인 영향을 미치는지 확인합니다.\n" +
            "식물을 자세히 관찰하고 특정 필요에 따라 점진적으로 조정하는 것을 기억하세요.";      //전체설명


    String shortExplan; // 짧은 설명
    String textExplan = explan;  //현재 화면에 나와야할 설명

    String temp,humid,light,ph,phos,pota,nitro,ec; //현재 온도,습도,조도,산화도,n,p,k,전기 전도도 값

    String name = "선인장"; //현재 이름


    BarChart bar_chart[] = new BarChart[8]; // 그래프들
    HashMap<String,BarChart> allBarChart; // 그래프들의 hashmap
    HashMap<String, Float> value = new HashMap<>();// 현재 식물의 상태값들



    public void null_judge(){
        if(temp == null){
            temp = "0";
        }
        if(humid == null){
            humid = "0";
        }if(light == null){
            light = "0";
        }if(nitro == null){
            nitro = "0";
        }if(ph == null){
            ph = "0";
        }if(phos== null){
            phos = "0";
        }if(pota == null){
            pota = "0";
        }
        if(ec == null){
            ec= "0";
        }

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

    @SuppressLint("SuspiciousIndentation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_2, container, false);

        new GetJsonDataTask().execute(MainActivity.URL);// 처음에 더미데이터 불러오기
        reset();//Value 초기화


        /*TextView trash = (TextView) view.findViewById(R.id.trash);
        trash.setText("");*/
       /* if(value == null){
            trash.setText("없음");
        }
        else{
            String t = ""+value.get("temp");
            trash.setText(t);
        }*/




        final String explan_1 = new String();

        Button btn = (Button) view.findViewById(R.id.button);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.button2);//닫기 버튼

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





        TextView textView1 = (TextView) view.findViewById(R.id.explan_text) ;//설명 text 적용
        Chat_gpt chatgpt = new Chat_gpt();//chat gpt 불러오기

        /*explan_1 = chatgpt.feedback(name,23.0,35.0,10.0,5.0,10.0,5.0,5.0,10000.0);
        explan = explan_1;
        textExplan = explan;*/
       /* TextView textView = view.findViewById(R.id.textView3);
        TextView textView2 = view.findViewById(R.id.textView6);
        TextView textView3 = view.findViewById(R.id.textView7);
        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        String recommandResponse = Chat_gpt.feedback("해바라기", 24.1, 17.2, 13, 5, 69, 5, 249, 6500);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(recommandResponse);
                            }
                        });
                    }
                }.start();
                new Thread(){
                    public void run(){
                        String recommandResponse = Chat_gpt.feedback("해바라기", 24.1, 17.2, 13, 5, 69, 5, 249, 6500);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView2.setText(recommandResponse);
                            }
                        });
                    }
                }.start();
                new Thread(){
                    public void run(){
                        String recommandResponse = Chat_gpt.feedback("해바라기", 24.1, 17.2, 13, 5, 69, 5, 249, 6500);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView3.setText(recommandResponse);
                            }
                        });
                    }
                }.start();
            }
        });
*/


        TextView textName = (TextView) view.findViewById(R.id.textView) ;//식물 이름
        textName.setText(name);//식물이름 적용





        Button btn_1 = view.findViewById(R.id.button4);//새로고침 버튼 추가
        mysrl = view.findViewById(R.id.content_srl);//당겨서 새로고침 레이아웃 적용

        View finalView = view;
        //new GetJsonDataTask().execute(MainActivity.URL);
        btn_1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new GetJsonDataTask().execute(MainActivity.URL);//새 더미데이터 가져오기
                //String t1 = ""+value.get("temp");






            }//새로고침 버튼을 클릭하면 새 더미데이터 가져옴
        });







        mysrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetJsonDataTask().execute(MainActivity.URL);//새 더미데이터 가지오기
                //null_judge();


                initChart(finalView,allBarChart.get("temp"),"온도",21,23);
                initChart(finalView,allBarChart.get("humid"),"습도",12.5f,20);
                initChart(finalView,allBarChart.get("light"),"조도",10000,10000);
                initChart(finalView,allBarChart.get("ph"),"PH",6.25F,5);
                initChart(finalView,allBarChart.get("nitro"),"N",11.5f,10);
                initChart(finalView,allBarChart.get("phos"),"P",5.5f,5);
                initChart(finalView,allBarChart.get("pota"),"k",11.5f,11);
                initChart(finalView,allBarChart.get("ec"),"EC",3,3);
                //그래프 그리기


                mysrl.setRefreshing(false);
            }//당겨서 새로고치면 더미데이터를 가져와 다시 그래프를 그리고, 표도 다시 그림
        });



        btn2.setVisibility(View.GONE);//닫기 버튼 비활성화

        getShortExplan(btn);// 설명 길이 판단및 짧은 설명 제작
        textView1.setText(textExplan) ;// 최종 현재 설명 적용

        if (btn.getVisibility() == View.VISIBLE) {
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    btn2.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.GONE);
                    textView1.setText(explan) ;

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



    public String gpt_view(View gptview) {
        Chat_gpt chatgpt = new Chat_gpt();
        TextView explan_textview = gptview.findViewById(R.id.explan_text);
        String explan_1 = chatgpt.feedback(name,23.0,35.0,10.0,5.0,10.0,5.0,5.0,10000.0);
        return explan_1;



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


    private class GetJsonDataTask extends AsyncTask<String, Void, HashMap<String, String>> {
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
                throw new RuntimeException(e);
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
            TextView nitroText =getView().findViewById(R.id.nitro);
            TextView phosText = getView().findViewById(R.id.phos);
            TextView potaText =getView().findViewById(R.id.pota);
            TextView ecText = getView().findViewById(R.id.ec);
            TextView rTxt = getView().findViewById(R.id.rTxt);
            if(temp == null){
                temp = "error";
            }
            tempText.setText(temp);
            humidText.setText(humid);
            lightText.setText(light);
            phText.setText(ph);
            nitroText.setText(nitro);
            phosText.setText(phos);
            potaText.setText(pota);
            ecText.setText(ec);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat=new SimpleDateFormat("마지막 업데이트 시간 : yyyy-MM-dd_HH:mm", Locale.CANADA);
            String dateTime = dateFormat.format(calendar.getTime());

            rTxt.setText(dateTime);
        }
    }

}