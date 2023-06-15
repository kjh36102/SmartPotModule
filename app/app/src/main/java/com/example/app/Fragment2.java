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

public class Fragment2 extends Fragment {
    public static HashMap<String, String> mDataHashMap;
    private static final String NAME_KEY = "name_key";
    private static final String TIPS = "tips";
    private static final String FEEDBACK = "feedbacks";
    private static final String SHORT_EXPLAN = "shortExplan";

    String explan, tips, improve, dateTime;
    String shortExplan, shortTips; 
    String textExplan = explan;  
    String tTips = tips;

    public static String temp, humid, ph, light, phos, pota, nitro, ec;
    
    @SuppressLint("SuspiciousIndentation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_2, container, false);

        TextView textView1 = (TextView) view.findViewById(R.id.explan_text);
        TextView textView2 = (TextView) view.findViewById(R.id.Tips);
        TextView textName = (TextView) view.findViewById(R.id.textView);
        TextView rTxt = (TextView) view.findViewById(R.id.rTxt);
        textName.setText(popup.plant);

        Button btn = (Button) view.findViewById(R.id.more_2);
        Button btn2 = (Button) view.findViewById(R.id.close_2);
        Button btn3 = (Button) view.findViewById(R.id.more);
        Button btn4 = (Button) view.findViewById(R.id.close);
        ImageButton update_btn = (ImageButton) view.findViewById(R.id.update);

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


        if (popup.plant.equals("")) {
            textName.setText("");
            textView1.setText("새로고침을 눌러 팁을 받아보세요!");
            textView2.setText("새로고침을 눌러 피드백을 받아보세요!");
            btn.setVisibility(View.GONE);
            btn2.setVisibility(View.GONE);
            btn3.setVisibility(View.GONE);
            btn4.setVisibility(View.GONE);
            rTxt.setVisibility(View.GONE);
        }
        else {
            rTxt.setVisibility(View.VISIBLE);

            if (MainActivity.sharedPreferences_fragment2.contains(FEEDBACK)) {
                rTxt.setText(MainActivity.sharedPreferences_fragment2.getString("datetime", ""));
                explan = MainActivity.sharedPreferences_fragment2.getString(FEEDBACK, "");
                shortExplan = MainActivity.sharedPreferences_fragment2.getString(SHORT_EXPLAN, "");
                btn.setVisibility(View.VISIBLE);
                textView1.setText(shortExplan);
                if (btn.getVisibility() == View.VISIBLE) {
                    btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            btn2.setVisibility(View.VISIBLE);
                            btn.setVisibility(View.GONE);
                            textView1.setText(explan);
                        }
                    });
                }
                btn2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        btn.setVisibility(View.VISIBLE);
                        btn2.setVisibility(View.GONE);
                        textView1.setText(shortExplan);
                    }
                });
            }
            if (MainActivity.sharedPreferences_fragment2.getString(NAME_KEY, "").equals(popup.plant)) {
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
                }
                btn4.setOnClickListener(new View.OnClickListener() {
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
            public void onClick(View v) {
                if(popup.plant.equals("")){
                    Toast.makeText(getContext(), "먼저 설정에서 식물 이름을 등록해주세요", Toast.LENGTH_SHORT).show();

                }
                else {
                    rTxt.setVisibility(View.VISIBLE);
                    textName.setText(popup.plant);
                    update_btn.setVisibility(View.INVISIBLE);

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
                    if (!MainActivity.sharedPreferences_fragment2.contains(NAME_KEY)) {
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
                    else if (!MainActivity.sharedPreferences_fragment2.getString(NAME_KEY, "").equals(popup.plant)) {
                        textView2.setText("...로딩중...");
                        btn3.setVisibility(View.GONE);
                        try {
                            gpt_tips(finalView);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        textName.setText(popup.plant);
                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                        editor.putString(NAME_KEY, popup.plant);
                        editor.apply();
                        if (popup.url != null && !popup.url.isEmpty()) {
                            new updateRequest().execute();
                            textView1.setText("...로딩중...");
                            btn.setVisibility(View.GONE);
                            btn2.setVisibility(View.GONE);
                        }
                        else{
                            textView1.setText("불러오기 실패, 등록을 다시한번 확인 해주세요");
                            btn.setVisibility(View.GONE);
                            btn2.setVisibility(View.GONE);
                            rTxt.setVisibility(View.GONE);
                        }
                    }

                    else if (MainActivity.sharedPreferences_fragment2.getString(NAME_KEY, "").equals(popup.plant)) {
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
                        }

                        btn4.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                btn3.setVisibility(View.VISIBLE);
                                btn4.setVisibility(View.GONE);
                                textView2.setText(shortTips);
                            }
                        });
                    }
                }
            }
        });
        return view;
    }

    public void getShortTips(Button btn){
        tTips = tips;
        if (tips.length() > 95) {
            shortTips = tips.substring(0, 92) + "..."; 
            btn.setVisibility(View.VISIBLE);
            tTips = shortTips;
        } else {
            shortTips = null;
            btn.setVisibility(View.GONE);
        }

    }

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
                if (parsed[0].equals("ok"))
                    return popup.url;
                 else if (parsed[0].equals("err"))
                    return null;
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
                Toast.makeText(getContext(), "측정 완료", Toast.LENGTH_SHORT).show();                
            } else
                Toast.makeText(getContext(), "측정 실패", Toast.LENGTH_SHORT).show();                
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
            ImageButton update_btn = getView().findViewById(R.id.update);
            Button btn1 = getView().findViewById(R.id.more_2);
            Button btn2 = getView().findViewById(R.id.close_2);
            rTxt.setText(dateTime);

            SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
            editor.putString("datetime", dateTime);
            editor.apply();

            if(null_judge()==true){
                textView1.setText("토양값이 불러와지지 않았습니다.");
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                rTxt.setVisibility(View.GONE);
            }
            else
                gpt_feedback(getView(), textView1, Double.parseDouble(temp), Double.parseDouble(humid), Double.parseDouble(light), Double.parseDouble(ph), Double.parseDouble(nitro), Double.parseDouble(phos), Double.parseDouble(pota), Double.parseDouble(ec));
        }
    }

    public boolean null_judge() {
        int i = 0;
        if (temp == null|| temp.equals("0") ) {
            temp = "0";
            i++;
        }
        if (humid == null|| humid.equals("0")) {
            humid = "0";
            i++;
        }
        if (light == null|| light.equals("0")) {
            light = "0";
            i++;
        }
        if (nitro == null|| nitro.equals("0")) {
            nitro = "0";
            i++;
        }
        if (ph == null|| ph.equals("0")) {
            ph = "0";
            i++;
        }
        if (phos == null|| phos.equals("0")) {
            phos = "0";
            i++;
        }
        if (pota == null|| pota.equals("0")) {
            pota = "0";
            i++;
        }
        if (ec == null|| ec.equals("0")) {
            ec = "0";
            i++;
        }
        if (i == 8)
            return true;
        else
            return false;

    }

    public void gpt_tips(View view) throws InterruptedException { 
        ChatGPT chatGPT = new ChatGPT();
        Button btn = (Button) view.findViewById(R.id.more);
        Button btn2 = (Button) view.findViewById(R.id.close);
        ImageButton update_btn = (ImageButton) view.findViewById(R.id.update);
        TextView textView1 = (TextView) view.findViewById(R.id.Tips);

        Thread thread = new Thread() {
            public void run() {
                getActivity().runOnUiThread(
                        () -> update_btn.setVisibility(View.GONE)
                );
                tips = chatGPT.tips(popup.plant);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                        editor.putString(TIPS, tips);
                        editor.apply();

                        btn2.setVisibility(View.GONE);
                        getShortTips(btn);
                        textView1.setText(tTips);

                        if (btn.getVisibility() == View.VISIBLE) {
                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    btn2.setVisibility(View.VISIBLE);
                                    btn.setVisibility(View.GONE);
                                    textView1.setText(tips);
                                }
                            });
                        }
                        btn2.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                btn.setVisibility(View.VISIBLE);
                                btn2.setVisibility(View.GONE);
                                textView1.setText(shortTips);
                            }
                        });
                    }
                });
                getActivity().runOnUiThread(
                        () -> update_btn.setVisibility(View.VISIBLE)
                );
            }

        };
        thread.start();
    }

    public void gpt_feedback(View view, TextView textView1, Double temp, Double humid, Double light, Double ph, Double nitro, Double phos, Double pota, Double ec) throws InterruptedException {
        ChatGPT chatgpt_1 = new ChatGPT();
        Button btn = (Button) view.findViewById(R.id.more_2);
        Button btn2 = (Button) view.findViewById(R.id.close_2);
        TextView trash1 = (TextView) view.findViewById(R.id.trash);
        TextView trash2 = (TextView) view.findViewById(R.id.trash2);
        ImageButton update_btn = (ImageButton) view.findViewById(R.id.update);

        Thread thread = new Thread() {
            public void run() {
                getActivity().runOnUiThread(
                        () -> update_btn.setVisibility(View.GONE)
                );
                String feedback_j = chatgpt_1.feedback(popup.plant, temp, humid, nitro, phos, pota, ph, ec, light);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(feedback_j);
                            explan = jsonObject.getString("분석");
                            shortExplan = jsonObject.getString("요약");
                            improve = jsonObject.getString("개선방안");
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "ChatGPT 결과 받기가 실패했습니다", Toast.LENGTH_SHORT).show();
                            return;
//                            throw new RuntimeException(e);
                        }
                        textExplan = "분석: " + explan + "\n\n개선방안: " + improve; 
                        shortExplan = "요약: " + shortExplan;

                        SharedPreferences.Editor editor = MainActivity.sharedPreferences_fragment2.edit();
                        editor.putString(FEEDBACK, textExplan);
                        editor.putString(SHORT_EXPLAN, shortExplan);
                        editor.apply();

                        btn2.setVisibility(View.GONE);
                        textView1.setText(shortExplan);
                        btn.setVisibility(View.VISIBLE);

                        if (btn.getVisibility() == View.VISIBLE) {
                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    btn2.setVisibility(View.VISIBLE);
                                    btn.setVisibility(View.GONE);
                                    textView1.setText(textExplan);
                                }
                            });
                        }

                        btn2.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                btn.setVisibility(View.VISIBLE);
                                btn2.setVisibility(View.GONE);
                                textView1.setText(shortExplan);
                            }
                        });
                    }
                });
                getActivity().runOnUiThread(
                        () -> update_btn.setVisibility(View.VISIBLE)
                );
            }

        };
        thread.start();
    }
}