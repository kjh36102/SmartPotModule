package com.example.app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String SHORT_EXPLAN = "shortExplan";
    private static final String IMPROVEMENT = "improve";




    String explan,tips,improve;
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





    @SuppressLint("SuspiciousIndentation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_2, container, false);

        TextView textView1 = (TextView) view.findViewById(R.id.explan_text);//설명 text 적용
        TextView textView2 = (TextView) view.findViewById(R.id.Tips);//tip text 적용
        TextView textName = (TextView) view.findViewById(R.id.textView) ;//식물 이름
        TextView rTxt = (TextView)view.findViewById(R.id.rTxt);
        textName.setText(popup.plant);//식물이름 적용



        Button btn = (Button) view.findViewById(R.id.more_2);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.close_2);//닫기 버튼
        Button btn3 = (Button) view.findViewById(R.id.more);//더보기 버튼
        Button btn4 = (Button) view.findViewById(R.id.close);//닫기 버튼
        ImageButton update_btn = (ImageButton)view.findViewById(R.id.update);//업데이트 버튼



        btn.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);
        btn4.setVisibility(View.GONE);
        update_btn.setVisibility(View.VISIBLE);


        TextView trash1 = view.findViewById(R.id.trash);//디버깅용 텍스트 박스 및 버튼
        TextView trash2 = view.findViewById(R.id.trash2);
        TextView trash3 = view.findViewById(R.id.trash3);
        Button button = view.findViewById(R.id.t_button);

        trash1.setVisibility(View.GONE);
        trash2.setVisibility(View.GONE);
        trash3.setVisibility(View.GONE);
        button.setVisibility(View.GONE);



        if(popup.plant.equals("")){//식물 이름이 정해지지 않았을 경우
            textName.setText("");
            textView1.setText("이름이 설정이 되지 않았습니다. 이름을 먼저 설정해주세요");
            textView2.setText("이름이 설정이 되지 않았습니다. 이름을 먼저 설정해주세요");
            btn.setVisibility(View.GONE);
            btn2.setVisibility(View.GONE);
            btn3.setVisibility(View.GONE);
            btn4.setVisibility(View.GONE);
            rTxt.setVisibility(View.GONE);

        }
        else {
            rTxt.setVisibility(View.VISIBLE);

            if (!MainActivity.sharedPreferences_fragment2.contains(FEEDBACK)) {//저장된 피드백이 없다면 새로 실행시켜 받아오기

                if (popup.url != null && !popup.url.isEmpty()) {//아두이노 IP를 알때만 사용가능
                    new updateRequest().execute();

                    textView1.setText("...로딩중...");
                    btn.setVisibility(View.GONE);//피드백 더보기, 닫기 버튼 비활성화
                    btn2.setVisibility(View.GONE);
                }
                else{
                    update_btn.setVisibility(View.VISIBLE);
                    textView2.setText("불러오기 실패, 등록을 다시한번 확인 해주세요");
                    btn.setVisibility(View.GONE);
                    btn2.setVisibility(View.GONE);

                    rTxt.setVisibility(View.GONE);


                }

            } else {//저장된 피드백이 있다면 받아오기
                rTxt.setText(MainActivity.sharedPreferences_fragment2.getString("datetime", ""));//마지막 업데이트일자 받아오기
                explan = MainActivity.sharedPreferences_fragment2.getString(FEEDBACK, "");//설명 받아오기
                shortExplan = MainActivity.sharedPreferences_fragment2.getString(SHORT_EXPLAN, "");//짧은 설명 받아오기
                btn.setVisibility(View.VISIBLE);//더보기 버튼 활성화

                textView1.setText(shortExplan);// 최종 현재 설명 적용
                if (btn.getVisibility() == View.VISIBLE) {//더보기 버튼이 활성화 됐을경우
                    btn.setOnClickListener(new View.OnClickListener() {//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화
                        public void onClick(View view) {
                            btn2.setVisibility(View.VISIBLE);
                            btn.setVisibility(View.GONE);
                            textView1.setText(explan);

                        }

                    });
                }


                btn2.setOnClickListener(new View.OnClickListener() {//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화
                    public void onClick(View view) {
                        btn.setVisibility(View.VISIBLE);
                        btn2.setVisibility(View.GONE);
                        textView1.setText(shortExplan);

                    }

                });
            }


            if (!MainActivity.sharedPreferences_fragment2.contains(NAME_KEY)) {//앱에 저장된 이름이 없을경우 실행시켜 이름을 새로 받아와 팁을 설정
                textView2.setText("...로딩중...");
                try {
                    gpt_tips(view);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                editor.putString(NAME_KEY, popup.plant);
                editor.apply();
            }//

            else if (!MainActivity.sharedPreferences_fragment2.getString(NAME_KEY, "").equals(popup.plant)) {//앱내에 저장된 이름과 현재 이름이 다를경우 다시 피드백과 팁 gpt에서 받아오기
                textView2.setText("...로딩중...");
                try {
                    gpt_tips(view);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                editor.putString(NAME_KEY, popup.plant);
                editor.apply();

                if (popup.url != null && !popup.url.isEmpty()) {//아두이노 IP를 알때만 사용가능
                    new updateRequest().execute();
                    textView1.setText("...로딩중...");
                    btn.setVisibility(View.GONE);
                    btn2.setVisibility(View.GONE);
                }
                else{
                    update_btn.setVisibility(View.VISIBLE);
                    textView2.setText("불러오기 실패, 등록을 다시한번 확인 해주세요");
                    btn.setVisibility(View.GONE);
                    btn2.setVisibility(View.GONE);

                    rTxt.setVisibility(View.GONE);

                }

            } else if (MainActivity.sharedPreferences_fragment2.getString(NAME_KEY, "").equals(popup.plant)) {//앱내에 저장된 이름과 현재이름이 같을 경우 저장된 피드백과 팁을 가져오기
                tips = MainActivity.sharedPreferences_fragment2.getString(TIPS, "");
                getShortTips(btn3);
                textView2.setText(tTips);
                if (btn3.getVisibility() == View.VISIBLE) {
                    btn3.setOnClickListener(new View.OnClickListener() {//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화
                        public void onClick(View view) {
                            btn4.setVisibility(View.VISIBLE);
                            btn3.setVisibility(View.GONE);
                            textView2.setText(tips);

                        }

                    });
                }


                btn4.setOnClickListener(new View.OnClickListener() {//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화
                    public void onClick(View view) {
                        btn3.setVisibility(View.VISIBLE);
                        btn4.setVisibility(View.GONE);
                        textView2.setText(shortTips);

                    }

                });

            }
        }

        View finalView = view;
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//업데이트 클릭시 실행
                if(popup.plant.equals("")){//이름이 비어있다면 실행
                    textName.setText("");
                    textView1.setText("이름이 설정이 되지 않았습니다. 이름을 먼저 설정해주세요");
                    textView2.setText("이름이 설정이 되지 않았습니다. 이름을 먼저 설정해주세요");
                    btn.setVisibility(View.GONE);
                    btn2.setVisibility(View.GONE);
                    btn3.setVisibility(View.GONE);
                    btn4.setVisibility(View.GONE);
                    rTxt.setVisibility(View.GONE);
                }
                else {
                    rTxt.setVisibility(View.VISIBLE);
                    textName.setText(popup.plant);
                    update_btn.setVisibility(View.INVISIBLE);//업데이트 클릭시 업데이트 버튼 사라짐, 각 함수가 실행되고 난뒤 다시 업데이트 버튼 활성화 시키기 위함


                    if (popup.url != null && !popup.url.isEmpty()) {
                        new updateRequest().execute();

                        textView1.setText("...로딩중...");
                        btn.setVisibility(View.GONE);
                        btn2.setVisibility(View.GONE);
                    }
                    else{
                        update_btn.setVisibility(View.VISIBLE);
                        textView1.setText("불러오기 실패, 등록을 다시한번 확인 해주세요");

                        btn.setVisibility(View.GONE);
                        btn2.setVisibility(View.GONE);

                        rTxt.setVisibility(View.GONE);


                    }
                    if (!MainActivity.sharedPreferences_fragment2.contains(NAME_KEY)) {//앱에 저장된 이름이 없을경우 실행시켜 이름을 새로 받아와 팁을 설정

                        textView2.setText("...로딩중...");
                        try {
                            gpt_tips(finalView);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                        editor.putString(NAME_KEY, popup.plant);
                        editor.apply();
                    }

                    else if (!MainActivity.sharedPreferences_fragment2.getString(NAME_KEY, "").equals(popup.plant)) {//앱내에 저장된 이름과 현재 이름이 다를경우 다시 피드백과 팁 gpt에서 받아오기
                        textView2.setText("...로딩중...");
                        btn3.setVisibility(View.GONE);
                        try {
                            gpt_tips(finalView);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        textName.setText(popup.plant);


                        if (popup.url != null && !popup.url.isEmpty()) {
                            new updateRequest().execute();
                            textView1.setText("...로딩중...");
                            btn.setVisibility(View.GONE);
                            btn2.setVisibility(View.GONE);
                        }
                        else{
                            update_btn.setVisibility(View.VISIBLE);
                            textView2.setText("불러오기 실패, 등록을 다시한번 확인 해주세요");
                            btn.setVisibility(View.GONE);
                            btn2.setVisibility(View.GONE);

                            rTxt.setVisibility(View.GONE);

                        }
                    }

                    else if (MainActivity.sharedPreferences_fragment2.getString(NAME_KEY, "").equals(popup.plant)) {//앱내에 저장된 이름과 현재이름이 같을 경우 저장된 팁을 가져오기
                        tips = MainActivity.sharedPreferences_fragment2.getString(TIPS, "");
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
                                textView2.setText(shortTips);

                            }

                        });//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화

                    }
                }



            }
        });//새로고침 버튼을 클릭하면 새 더미데이터 가져옴






        return view;
    }


    public void getShortTips(Button btn){
        tTips = tips;
        if(tips.length()>95){
            shortTips = tips.substring(0,92)+"..."; // 설명을 세줄로 자르고 뒤에 ...으로 설명을 덧붙임
            btn.setVisibility(View.VISIBLE);//더보기 버튼 등장
            tTips = shortTips;//현재 설명에 짧은 설명으로 적용
        }
        else{
            shortTips = null;
            btn.setVisibility(View.GONE);//더보기 버튼 삭제
        }//설명이 짧을 경우 짧은 설명은 NULL

    }//짧은 설명 판단 및 만드는 함수

    private class updateRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(popup.url+"measureNow").openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseData.append(line);
                }
                reader.close();

                String parsed[] = responseData.toString().split("\\|");
                if (parsed[0].equals("ok")) {
                    return popup.url;
                } else if (parsed[0].equals("err")) {
                    return null;
                }
                connection.disconnect();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                new GetJsonDataTask().execute(result);
                Toast.makeText(getContext(), "측정 완료", Toast.LENGTH_SHORT).show();                // 측정 완료 toast메시지 출력
            } else {
                Toast.makeText(getContext(), "측정 실패", Toast.LENGTH_SHORT).show();                // 측정 실패 toast메시지 출력
            }
        }
    }
    public class GetJsonDataTask extends AsyncTask<String, Void, HashMap<String, String>> {
        @Override
        protected HashMap<String, String> doInBackground(String... urls) {
            mDataHashMap=null;
            HashMap<String, String> resultHashMap = new HashMap<>();
            try {

                URL url = new URL(urls[0]+"getTableData?name=soil_data");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder responseData = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    responseData.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                String parsed[] = responseData.toString().split("\\|");
                if (parsed[0].equals("ok") && parsed[1].equals("0")) {
                    String dataString = parsed[2];
                    org.json.JSONArray jsonArray = new JSONArray(dataString);
                    if (jsonArray.length() > 0) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        resultHashMap.put("temp", jsonObject.getString("tm"));
                        resultHashMap.put("humid", jsonObject.getString("hm"));
                        resultHashMap.put("light", jsonObject.getString("lt"));
                        resultHashMap.put("ph", jsonObject.getString("ph"));
                        resultHashMap.put("nitro", jsonObject.getString("n"));
                        resultHashMap.put("phos", jsonObject.getString("p"));
                        resultHashMap.put("pota", jsonObject.getString("k"));
                        resultHashMap.put("ec", jsonObject.getString("ec"));
                        resultHashMap.put("ts", jsonObject.getString("ts"));
                    }
                }
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
            dateTime = mDataHashMap.get("ts");
            dateTime = "마지막 업데이트 시간 :"+dateTime;
            TextView rTxt = getView().findViewById(R.id.rTxt);
            TextView textView1 = getView().findViewById(R.id.explan_text);


            rTxt.setText(dateTime);



            SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();

            editor.putString("datetime", dateTime);
            editor.apply();

            rTxt.setText(dateTime);
            ChatGPT chatGPT = new ChatGPT();



            gpt_feedback(getView(),textView1, Double.parseDouble(temp), Double.parseDouble(humid), Double.parseDouble(light), Double.parseDouble(ph), Double.parseDouble(nitro), Double.parseDouble(phos), Double.parseDouble(pota), Double.parseDouble(ec));




        }
    }



    public void gpt_tips(View view) throws InterruptedException { //tip써주는 함수
        ChatGPT chatGPT = new ChatGPT();
        Button btn = (Button) view.findViewById(R.id.more);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.close);//닫기 버튼
        ImageButton update_btn = (ImageButton) view.findViewById(R.id.update) ;
        TextView textView1 = (TextView) view.findViewById(R.id.Tips);//tip 텍스트뷰

        Thread thread = new Thread() {
            public void run() {

                tips = chatGPT.tips(popup.plant);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();//팁을 따로 앱에 저장
                        editor.putString(TIPS, tips);
                        editor.apply();

                        btn2.setVisibility(View.GONE);//닫기 버튼 비활성화

                        getShortTips(btn);//팁을 줄여주게 만들어줌
                        textView1.setText(tTips);// 최종 현재 설명 적용

                        if (btn.getVisibility() == View.VISIBLE) {
                            btn.setOnClickListener(new View.OnClickListener() {//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화
                                public void onClick(View view) {
                                    btn2.setVisibility(View.VISIBLE);
                                    btn.setVisibility(View.GONE);
                                    textView1.setText(tips);

                                }

                            });
                        }


                        btn2.setOnClickListener(new View.OnClickListener() {//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화
                            public void onClick(View view) {
                                btn.setVisibility(View.VISIBLE);
                                btn2.setVisibility(View.GONE);
                                textView1.setText(shortTips);

                            }

                        });
                    }
                });
            }

        };
        thread.start();
        thread.join();

        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
        editor.putString(NAME_KEY, popup.plant);
        editor.apply();
        update_btn.setVisibility(View.VISIBLE);
    }//chat gpt 적용하여 피드백 가져오기






    public void gpt_feedback(View view, TextView textView1, Double temp, Double humid, Double light, Double ph, Double nitro, Double phos, Double pota, Double ec) throws InterruptedException {
//chat gpt 적용하여 피드백 가져오기
        ChatGPT chatgpt_1 = new ChatGPT();
        Button btn = (Button) view.findViewById(R.id.more_2);//더보기 버튼
        Button btn2 = (Button) view.findViewById(R.id.close_2);//닫기 버튼
        TextView trash1 = (TextView)view.findViewById(R.id.trash);//디버깅용 텍스트뷰
        TextView trash2 = (TextView)view.findViewById(R.id.trash2);
        ImageButton update_btn = (ImageButton) view.findViewById(R.id.update);//업데이트 버튼

        btn.setVisibility(View.VISIBLE);//더보기 버튼 활성화



        Thread thread = new Thread() {
            public void run() {

                String feedback_j = chatgpt_1.feedback(popup.plant, temp, humid, nitro, phos, pota, ph, ec, light);//gpt에서 피드백 가져오기

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        try {//gpt에서 받아온 json형식 문자열을 json으로 변환후 각 key값에 맞춰서 문자열로 따로 저장
                            JSONObject jsonObject = new JSONObject(feedback_j);
                            explan = jsonObject.getString("분석");
                            shortExplan = jsonObject.getString("요약");
                            improve = jsonObject.getString("개선방안");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        textExplan = "분석: "+explan+"\n\n개선방안: "+improve; //분석+개선방안 으로 긴 피드백 설정
                        shortExplan = "요약: "+shortExplan;//요약으로 짧은 피드백 설정

                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                        editor.putString(FEEDBACK, textExplan);//긴 피드백과 짧은 피드백 앱내에 저장
                        editor.putString(SHORT_EXPLAN, shortExplan);
                        editor.apply();


                        btn2.setVisibility(View.GONE);//닫기 버튼 비활성화


                        textView1.setText(shortExplan);// 최종 현재 설명 적용

                        if (btn.getVisibility() == View.VISIBLE) {//더보기 버튼이 있고, 클릭이 되면 닫기 버튼 활성화및 더보기 버튼 비활성화
                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    btn2.setVisibility(View.VISIBLE);
                                    btn.setVisibility(View.GONE);
                                    textView1.setText(textExplan);

                                }

                            });
                        }


                        btn2.setOnClickListener(new View.OnClickListener() {//닫기 버튼을 누르면 더보기 버튼 활성화 및 닫기 버튼 비활성화
                            public void onClick(View view) {
                                btn.setVisibility(View.VISIBLE);
                                btn2.setVisibility(View.GONE);
                                textView1.setText(shortExplan);

                            }

                        });
                    }
                });
            };

        };

        thread.start();

        thread.join();


        update_btn.setVisibility(View.VISIBLE);

    }
}