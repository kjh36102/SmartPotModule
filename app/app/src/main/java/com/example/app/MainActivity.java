package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    TabPagerAdapter adapter;
    String[] tabName = new String[] {"대시보드", "상세분석", "식물관리"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar =(Toolbar)findViewById(R.id.toolbar);           // actionbar에서 toolbar로 변경
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle("SmartPotModule");

        //xml 연결
        tabLayout=findViewById(R.id.tabs);
        viewPager=findViewById(R.id.pager);
        //adapter 준비 및 연결
        adapter = new TabPagerAdapter(this);
        viewPager.setAdapter(adapter);
        // TabLayout, ViewPager 연결
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                TextView textView = new TextView(MainActivity.this);
                textView.setText(tabName[position]);
                textView.setTextSize(18);
                textView.setGravity(Gravity.CENTER);
                tab.setCustomView(textView);
            }
        }).attach();

        findViewById(R.id.wifi_button).setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.wifi_button:
                startActivity(new Intent(this, popup.class));

                break;
        }
    }
}