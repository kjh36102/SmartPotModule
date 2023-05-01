package com.example.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment3 extends Fragment {
    private TabLayout tabLayout;
    private TextView tvValue,tvRange,tvValue1,tvValue2,tvValue3,tvValue4,tvValue5,tvValue6,tvValue7,tvValue8;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_mode);
        tvValue = view.findViewById(R.id.tv_value);
        tvRange = view.findViewById(R.id.tv_range);
        tvValue1 = view.findViewById(R.id.tv_value_1);
        tvValue2 = view.findViewById(R.id.tv_value_2);
        tvValue3 = view.findViewById(R.id.tv_value_3);
        tvValue4 = view.findViewById(R.id.tv_value_4);
        tvValue5 = view.findViewById(R.id.tv_value_5);
        tvValue6 = view.findViewById(R.id.tv_value_6);
        tvValue7 = view.findViewById(R.id.tv_value_7);
        tvValue8 = view.findViewById(R.id.tv_value_8);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            // Tab이 선택되었을 때
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 선택된 Tab의 위치를 가져옴
                int position = tab.getPosition();

                Log.i("##INFO", "onTabSelected(): position = "+position);
                switch (position) {
                    case 0 :
                        changeText("희망값","임계범위","1","25","5","08:00","15","5","16:00","10");
                        break;
                    case 1 :
                        changeText("조도값","감지시간","600","60","2","08:00","on","2","20:00","off");
                        break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void changeText(String value, String range, String value1,String value2,String value3,String value4,String value5,String value6,String value7,String value8) {
        tvValue.setText(value);
        tvRange.setText(range);
        tvValue1.setText(value1);
        tvValue2.setText(value2);
        tvValue3.setText(value3);
        tvValue4.setText(value4);
        tvValue5.setText(value5);
        tvValue6.setText(value6);
        tvValue7.setText(value7);
        tvValue8.setText(value8);
    }
}