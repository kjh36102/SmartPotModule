package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import java.util.Iterator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment1 extends Fragment{
    public static HashMap<String, String> mDataHashMap;
    public static int score;
    public static ImageView smileface;
    public static ImageView noface ;
    public static ImageView angryface;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_1, container, false);
        smileface=view.findViewById(R.id.face1);
        noface = view.findViewById(R.id.face2);
        angryface =view.findViewById(R.id.face3);
        setFace();
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ToggleButton toggleButton = view.findViewById(R.id.toggleButton);
        Button rBtn = view.findViewById(R.id.rButton);
        Button water = view.findViewById(R.id.nowWater);
        CheckBox waterCK = view.findViewById(R.id.check_mode_water);
        CheckBox lightCK = view.findViewById(R.id.check_mode_light);

        rBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new GetJsonDataTask().execute(MainActivity.URL);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setFace();
                    }
                }, 5000); // 3초 뒤에 setFace() 실행
            }
        });
        water.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //물키는기능
            }
        });
        toggleButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(toggleButton.isChecked()==true){
                    //켜졌을때 동작할거
                }
                else {
                    //꺼짐기능 코드
                }
            }
        });
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {toggleButton.setBackgroundColor(Color.rgb(255,165,0));}
                else {toggleButton.setBackgroundColor(Color.LTGRAY);}
            }
        });
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
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat=new SimpleDateFormat("마지막 업데이트 시간 : yyyy-MM-dd_HH:mm");
            String dateTime = dateFormat.format(calendar.getTime());
            rTxt.setText(dateTime);

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
                                score = Integer.parseInt(scoreString);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }.start();

        }
    }

    public void setFace(){
        setBlackImage(smileface, R.drawable.smileface1);
        setBlackImage(noface, R.drawable.noface1);
        setBlackImage(angryface, R.drawable.angryface1);

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