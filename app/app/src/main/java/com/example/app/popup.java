package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class popup extends AppCompatActivity implements View.OnClickListener {
    private String ssid;
    private String pw;
    private int port;
    private String mac;
    private String ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //타이틀 미사용
        setContentView(R.layout.activity_popup);
        findViewById(R.id.btnClose).setOnClickListener((View.OnClickListener) this);
        Button regiBtn = findViewById(R.id.btnregister);
        regiBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getText();
                register(ssid,pw,port);
                setText();
            }
        });
    }
    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnClose:
                this.finish();
                break;
        }
    }
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {return false;}
        return true;
    }  //팝업 영역 밖 클릭시 닫힘 방지

    public void getText(){
        ssid=pw=mac=ip=null;
        port=-1;
        EditText editSsid = findViewById(R.id.in_ssid);
        EditText editPw = findViewById(R.id.in_pw);
        EditText editPort = findViewById(R.id.in_port);
        ssid = editSsid.getText().toString();
        pw = editPw.getText().toString();
        port = Integer.parseInt(editPort.getText().toString());
    }
    public void register(String SSID, String pw, int port){
        //SSID, PW. PORT값 제품으로 넘기고 제품 MAC가져오는 기능
        //기존 입력값 제거하려면 코드 추가
    }
    public void setText(){
        TextView macText = findViewById(R.id.show_mac);
        TextView ipText = findViewById(R.id.show_ip);
        // MAC으로 IP검색 후 ip에 출력하는 코드
        macText.setText(mac);
        ipText.setText(ip);
    }
}