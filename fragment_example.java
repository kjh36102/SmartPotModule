package com.example.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.simple.JSONObject;


public class Fragment2 extends Fragment {
    TextView textView;
    TextView textView2;
    TextView textView3;
    TextView textView4;

    Button button;
    Button button2;
    Button button3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_2, container, false);

        ChatGPT chatGPT = new ChatGPT();

        textView = view.findViewById(R.id.textView);
        textView2 = view.findViewById(R.id.textView2);
        textView3 = view.findViewById(R.id.textView3);
        textView4 = view.findViewById(R.id.textView4);

        button = view.findViewById(R.id.button);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        JSONObject scoreResponse = chatGPT.score("선인장", 20, 60, 120, 60, 100, 6, 400, 5000);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView2.setText(scoreResponse.toJSONString());
                            }
                        });
                    }
                }.start();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread() {
                    public void run () {
                        String feedbackResponse = chatGPT.feedback("선인장", 20, 60, 120, 60, 100, 6, 400, 5000);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView3.setText(feedbackResponse);
                            }
                        });
                    }
                }.start();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread() {
                    public void run () {
                        String tipResponse = chatGPT.tips("볼부 선인장");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView4.setText(tipResponse);
                            }
                        });
                    }
                }.start();
            }
        });
        return view;
    }
}
