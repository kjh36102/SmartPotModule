package com.example.mysampleservice;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    TextView textView;
    TextView textView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChatGPT chatGPT = new ChatGPT("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");
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼 눌렀을 때 network를 사용하는 API인 경우 Thread로 처리해야 함
                new Thread() {
                    public void run() {
                        String feedbackResponse = chatGPT.feedback("해바라기", 24.1, 17.2, 13, 5, 69, 5, 249);
                        //response를 받는 즉시 스마트폰 UI에 해당되는 값을 적용할 때 다른 Thread에서는 변경을 못해줌
                        //메인 쓰레드에서 변경을 할 수 있음
                        runOnUiThread(new Runnable() { //메인쓰레드에서 코드를 실행할 수 있도록 해주는 메서드
                            @Override
                            public void run() {
                                textView.setText(feedbackResponse); // UI상에 적용
                            }
                        });
                    }
                }.start();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        String recommandResponse = chatGPT.recommand("해바라기");
                        //response를 받는 즉시 스마트폰 UI에 해당되는 값을 적용할 때 다른 Thread에서는 변경을 못해줌
                        //메인 쓰레드에서 변경을 할 수 있음
                        runOnUiThread(new Runnable() { //메인쓰레드에서 코드를 실행할 수 있도록 해주는 메서드
                            @Override
                            public void run() {
                                textView2.setText(recommandResponse); // UI상에 적용
                            }
                        });
                    }
                }.start();
            }
        });
    }
}
