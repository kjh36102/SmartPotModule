package com.example.mysampleservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChatGPT chatGPT = new ChatGPT("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");
//        String response;
        textView = findViewById(R.id.textView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼 눌렀을 때 network를 사용하는 API인 경우 Thread로 처리해야 함
                new Thread() {
                    public void run() {
                        String response = chatGPT.Chat("저녁 메뉴 추천점");
                        //response를 받는 즉시 스마트폰 UI에 해당되는 값을 적용할 때 다른 Thread에서는 변경을 못해줌
                        //메인 쓰레드에서 변경을 할 수 있음
                        runOnUiThread(new Runnable() { //메인쓰레드에서 코드를 실행할 수 있도록 해주는 메서드
                            @Override
                            public void run() {
                                textView.setText(response); // UI상에 적용
                            }
                        });
                    }
                }.start();
            }
        });
    }
}
