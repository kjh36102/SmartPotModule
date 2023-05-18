package com.example.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Printer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;


public class Fragment3 extends Fragment {
    private TabLayout tabLayout;
    private TableLayout tableCenter;
    private View includeView;
    private TextView tvCheck, tvValue, tvRange, tvValue1, tvValue2, tvValue3, tvValue4, tvValue5, edValue1,edValue2, tvValue7, tvValue8, timeToStatus;
    private CheckBox checkBox;
    private String curType = WATER;

    private Button btDelete, btRegister;
    private DataAdapter dataAdapter;

    private static ArrayList<DataValue> waterDataList = null;
    private static ArrayList<DataValue> lightDataList = null;
    private ArrayList<DataValue> deleteList;

    private RecyclerView reData;

    private EditText value1, value2, value3;
    private static final String WATER = "water";
    private static final String LIGHT = "light";

    private static Boolean isWaterCheck = false;
    private static Boolean isLightCheck = false;

    private static String edWater1 = "";
    private static String edWater2 = "";
    private static String edLight1 = "";
    private static String edLight2 = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCheck = view.findViewById(R.id.tv_check_name);
        tabLayout = view.findViewById(R.id.tab_mode);
        tableCenter = view.findViewById(R.id.tab_layout_center);
        tvValue = view.findViewById(R.id.tv_want_value);
        tvRange = view.findViewById(R.id.tv_range);
        includeView = view.findViewById(R.id.in_uncheck);
        tvValue1 = view.findViewById(R.id.tv_value_1);
        tvValue2 = view.findViewById(R.id.tv_value_2);
        checkBox = view.findViewById(R.id.check_mode);
        tvValue3 = includeView.findViewById(R.id.tv_time_to_status);
        btDelete = includeView.findViewById(R.id.bt_delete);
        btRegister = includeView.findViewById(R.id.bt_register);
        reData = includeView.findViewById(R.id.rv_center_data);

        value1 = includeView.findViewById(R.id.ed_value_1);
        value2 = includeView.findViewById(R.id.ed_value_2);
        value3 = includeView.findViewById(R.id.ed_value_3);
        tvValue4 = includeView.findViewById(R.id.tv_a);
        tvValue5 = includeView.findViewById(R.id.tv_b);

        edValue1 = includeView.findViewById(R.id.ed_input_1);
        edValue2 = includeView.findViewById(R.id.ed_input_2);


        //리사이클러뷰를 담담하는 어댑터 초기화
        waterDataList = new ArrayList<>();
        lightDataList = new ArrayList<>();
        deleteList = new ArrayList<>();
        dataAdapter = new DataAdapter(waterDataList);

        //리사이클러뷰 초기화 진행
        reData.setAdapter(dataAdapter);
        reData.setLayoutManager(new LinearLayoutManager(getContext()));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            // Tab이 선택되었을 때
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 선택된 Tab의 위치를 가져옴
                int position = tab.getPosition();
                value1.setText("");
                value2.setText("");
                value3.setText("");

                switch (position) {
                    case 0:
                        curType = WATER;
                        Log.i("##INFO", "onTabSelected(): iswaterCheck: " + isWaterCheck);
                        if (isWaterCheck) {
                            checkBox.setChecked(true);
                        } else {
                            checkBox.setChecked(false);
                        }
                        if (edWater1 != null || edWater2 != null) {
                            edValue1.setText(edWater1);
                            edValue2.setText(edWater2);
                        } else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }
                        changeText("희망값", "임계범위", "1", "25", "급수시간", "A", "B");
                        dataAdapter.notifyItemRangeRemoved(0, waterDataList.size());
                        dataAdapter.setDataList(waterDataList);

                        break;
                    case 1:
                        curType = LIGHT;
                        if (isLightCheck) {
                            checkBox.setChecked(true);
                        } else {
                            checkBox.setChecked(false);
                        }
                        if (edLight1 != null || edLight2 != null) {
                            edValue1.setText(edLight1);
                            edValue2.setText(edLight2);
                        } else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }
                        changeText("조도값", "감지시간", "600", "60", "조명상태", "C", "D");
                        dataAdapter.setDataList(lightDataList);
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


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (curType.equals(WATER)) {
                        isWaterCheck = true;
                    } else {
                        isLightCheck = true;
                    }
                    includeView.setVisibility(View.GONE);
                    tableCenter.setVisibility(View.VISIBLE);
                    tvValue1.setText("");
                    tvValue2.setText("");
                } else {
                    Log.i("##INFO", "onCheckedChanged(): ???");
                    if (curType.equals(WATER)) {
                        isWaterCheck = false;
                    } else {
                        isLightCheck = false;
                    }
                    includeView.setVisibility(View.VISIBLE);
                    tableCenter.setVisibility(View.GONE);
                    tvValue1.setText("");
                    tvValue2.setText("");
                }
            }
        });


        //등록부분
        btRegister.setOnClickListener(v -> {
            if (Objects.equals(curType, WATER)) {
                //최대 6개까지만 입력 가능
                if (waterDataList.size() < 7) {
                    DataValue dataValue = new DataValue();

                    dataValue.setDate(value1.getText().toString());
                    dataValue.setTime(value2.getText().toString());
                    dataValue.setValue(value3.getText().toString());

                    waterDataList.add(dataValue);
                    dataAdapter.setDataList(waterDataList);
                }
            } else {
                //최대 6개까지만 입력 가능
                if (lightDataList.size() < 7) {
                    DataValue dataValue = new DataValue();

                    dataValue.setDate(value1.getText().toString());
                    dataValue.setTime(value2.getText().toString());
                    dataValue.setValue(value3.getText().toString());

                    lightDataList.add(dataValue);
                    dataAdapter.setDataList(lightDataList);
                }

            }
        });


        //삭제버튼 클릭 이벤트 부분
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < deleteList.size(); i++) {
                    Log.i("##INFO", "onClick(): deleteList.get(i);"+deleteList.get(i));
                }
                dataAdapter.removeData(deleteList);
                deleteList = new ArrayList<>();
            }
        });

        dataAdapter.setItemClickCallback(new DataAdapter.ItemClickCallback() {
            @Override
            public void onClick(int position) {

                ArrayList<DataValue> list = new ArrayList<>();
                if (curType.equals(WATER)){
                    list = waterDataList;
                }else {
                    list = lightDataList;
                }

                if (deleteList.contains(list.get(position))) {
                    Log.i("##INFO", "onClick(): remove");
                    deleteList.remove(list.get(position));
                } else {
                    Log.i("##INFO", "onClick(): add = " + list.get(position).date);
                    deleteList.add(list.get(position));
                }
            }
        });


        edValue1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER)) {
                    edWater1 = edValue1.getText().toString();
                } else {
                    edLight1 = edValue1.getText().toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("##INFO", "afterTextChanged(): editable = " + editable);
            }
        });

        edValue2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER)) {
                    edWater2 = edValue2.getText().toString();
                } else {
                    edLight2 = edValue2.getText().toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("##INFO", "afterTextChanged(): editable = " + editable);
            }
        });



    }


    private void changeText(String value, String range, String value1, String value2, String value3, String value4, String value5) {
        tvValue.setText(value);
        tvRange.setText(range);
        tvValue3.setText(value3);
        tvValue4.setText(value4);
        tvValue5.setText(value5);
    }
}