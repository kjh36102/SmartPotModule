package com.example.app;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;

public class Fragment1 extends Fragment{
    public static HashMap<String, String> mDataHashMap;
    public static float score;
    public static ImageView smileface,noface, angryface;
    Button rBtn;
    AppCompatButton waterBtn;
    ToggleButton toggleButton;
    boolean water,light0, light1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_1, container, false);
        smileface=view.findViewById(R.id.face1);
        noface = view.findViewById(R.id.face2);
        angryface =view.findViewById(R.id.face3);
        setBlack();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        rBtn = view.findViewById(R.id.rButton);
        waterBtn = view.findViewById(R.id.nowWater);
        toggleButton = view.findViewById(R.id.toggleButton);
        viewModel.getWaterState().observe(getViewLifecycleOwner(), state -> {
            waterBtn.setEnabled(state ? false : true);
        });
        viewModel.getLightState().observe(getViewLifecycleOwner(), state -> {
            toggleButton.setEnabled(state ? false : true);
        });

        rBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (popup.url != null && !popup.url.isEmpty()) 
                        new updateRequest().execute();
            }
        });
        waterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new Thread (()->{
                    try {
                        URL url = new URL(popup.url + "water");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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

                        if( parsed[0].equals("ok")) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "급수 완료", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else if(parsed[0].equals("err")){
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "급수 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
        toggleButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(toggleButton.isChecked()==true){
                    new Thread (()->{
                        try {
                            URL url = new URL(popup.url + "controlLight?state=true");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                            if( parsed[0].equals("ok")) {
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "조명 켜기 완료", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else if(parsed[0].equals("err")){
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "조명 켜기 실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (SocketTimeoutException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                else {
                    new Thread (()->{
                        try {
                            URL url = new URL(popup.url + "controlLight?state=false");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                            if( parsed[0].equals("ok")) {
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "조명 끄기 완료", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else if(parsed[0].equals("err")){
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "조명 끄기 실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (SocketTimeoutException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    toggleButton.setBackgroundColor(Color.rgb(255,165,0));
                else
                    toggleButton.setBackgroundColor(Color.LTGRAY);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new updateToggleButtonState().execute();
    }
    private class updateToggleButtonState extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url2 = new URL(popup.url+"getTableData?name=plant_manage");
                HttpURLConnection httpURLConnection2 = (HttpURLConnection) url2.openConnection();

                InputStream inputStream2 = httpURLConnection2.getInputStream();
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                String line2;
                StringBuilder responseData2 = new StringBuilder();
                while ((line2 = bufferedReader2.readLine()) != null) {
                    responseData2.append(line2);
                }
                bufferedReader2.close();
                inputStream2.close();
                httpURLConnection2.disconnect();

                String parsed2[] = responseData2.toString().split("\\|");
                if (parsed2[0].equals("ok") && parsed2[1].equals("0")) {
                    String dataString2 = parsed2[2];
                    JSONArray jsonArray2 = new JSONArray(dataString2);
                    if (jsonArray2.length() > 0) {
                        JSONObject jsonObject2 = jsonArray2.getJSONObject(0); //자동=1, 수동=0
                            if (jsonObject2.optString("l_on").equals("1"))
                                light1=true;
                            else if (jsonObject2.optString("l_on").equals("0"))
                                light1=false;
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            toggleButton.setChecked(light1);
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
    private class GetJsonDataTask extends AsyncTask<String, Void, HashMap<String, String>> {
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
                    JSONArray jsonArray = new JSONArray(dataString);
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

                URL url2 = new URL(urls[0]+"getTableData?name=plant_manage");
                HttpURLConnection httpURLConnection2 = (HttpURLConnection) url2.openConnection();

                InputStream inputStream2 = httpURLConnection2.getInputStream();
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                String line2;
                StringBuilder responseData2 = new StringBuilder();
                while ((line2 = bufferedReader2.readLine()) != null) {
                    responseData2.append(line2);
                }
                bufferedReader2.close();
                inputStream2.close();
                httpURLConnection2.disconnect();

                String parsed2[] = responseData2.toString().split("\\|");
                if (parsed2[0].equals("ok") && parsed2[1].equals("0")) {
                    String dataString2 = parsed2[2];
                    JSONArray jsonArray2 = new JSONArray(dataString2);
                    if (jsonArray2.length() > 0) {
                        JSONObject jsonObject2 = jsonArray2.getJSONObject(0); 
                        if (jsonObject2.optString("w_auto").equals("0"))
                            water = true;   
                        else if (jsonObject2.optString("w_auto").equals("1"))
                            water = false;
                        if (jsonObject2.optString("l_auto").equals("0")) {
                            light0=true;
                            if (jsonObject2.optString("l_on").equals("1"))
                                light1=true;
                            else if (jsonObject2.optString("l_on").equals("0"))
                                light1=false;
                        }
                        else if (jsonObject2.optString("l_auto").equals("1")){
                            light0=false;
                            if (jsonObject2.optString("l_on").equals("1"))
                                light1 = true;
                            else if (jsonObject2.optString("l_on").equals("0"))
                                light1 = false;
                        }
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
                if(mDataHashMap == null) throw new NullPointerException();    //튕기지는 않게 하려고 널체크 추가함
                updateDataTextView();
            }catch(Exception e){
                e.printStackTrace();
            }
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
            TextView tempText = getView().findViewById(R.id.temp);
            TextView humidText = getView().findViewById(R.id.humid);
            TextView lightText = getView().findViewById(R.id.light);
            TextView phText = getView().findViewById(R.id.ph);
            TextView nitroText =getView().findViewById(R.id.nitro);
            TextView phosText = getView().findViewById(R.id.phos);
            TextView potaText =getView().findViewById(R.id.pota);
            TextView ecText = getView().findViewById(R.id.ec);
            TextView rTxt = getView().findViewById(R.id.rText);
            tempText.setText(temp);
            humidText.setText(humid);
            lightText.setText(light);
            phText.setText(ph);
            nitroText.setText(nitro);
            phosText.setText(phos);
            potaText.setText(pota);
            ecText.setText(ec);
            if(ts != null)
                rTxt.setText("마지막 업데이트 시간 : " + ts);  
            else
                rTxt.setText("");
            waterBtn.setEnabled(water);
            toggleButton.setEnabled(light0);
            toggleButton.setChecked(light1);
            Fragment3.humid = humid;
            Fragment3.light = light;


            if (!popup.plant.equals("")) {
                ChatGPT chatGPT = new ChatGPT();
                new Thread() {
                    public void run() {
                        org.json.simple.JSONObject scoreResponse = chatGPT.score(popup.plant, Double.parseDouble(temp), Double.parseDouble(humid), Double.parseDouble(nitro), Double.parseDouble(phos), Double.parseDouble(pota), Double.parseDouble(ph), Double.parseDouble(ec), Double.parseDouble(light));
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
            else{
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "식물이름 등록바람", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    public void setBlack(){
        setBlackImage(smileface, R.drawable.smileface1);
        setBlackImage(noface, R.drawable.noface1);
        setBlackImage(angryface, R.drawable.angryface1);
    }

    public void setFace(){
        setBlack();
        try{
            if(score >=80)
                setColorImage(smileface, R.drawable.smileface);
            else if(score >=50)
                setColorImage(noface, R.drawable.noface);
            else
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
}