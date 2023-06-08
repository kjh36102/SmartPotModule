package com.example.app;

import com.example.app.Fragment1;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
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
import java.util.function.ObjIntConsumer;


public class Fragment3 extends Fragment {
    private TabLayout tabLayout;
    private TableLayout tableCenter;
    private View includeView;
    private TextView tvCheck, tvValue, tvRange, tvValue1, tvValue2, tvValue3, tvValue4, tvValue5, edValue1,edValue2, tvLight, tvHumid;
    private static CheckBox checkBoxWater, checkBoxLight;
    private Button waterButton;
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


    private static String edWater1 = "";
    private static String edWater2 = "";
    private static String edLight1 = "";
    private static String edLight2 = "";


    private String waterValue1 = "";
    private String waterValue2 = "";
    private String lightValue1 = "";
    private String lightValue2 = "";
    public static String humid = "";
    public static String light = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.i("##INFO", "onCreateView(): fragment3");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_3, container, false);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        tvHumid.setText(humid);
        tvLight.setText(light);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("##INFO", "onViewCreated(): Fragment3");
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        tvCheck = view.findViewById(R.id.tv_check_name);
        tabLayout = view.findViewById(R.id.tab_mode);
        tableCenter = view.findViewById(R.id.tab_layout_center);
        tvValue = view.findViewById(R.id.tv_want_value);
        tvRange = view.findViewById(R.id.tv_range);
        includeView = view.findViewById(R.id.in_uncheck);
        tvValue1 = view.findViewById(R.id.tv_value_1);
        tvValue2 = view.findViewById(R.id.tv_value_2);
        checkBoxWater = view.findViewById(R.id.check_mode_water);
        checkBoxLight = view.findViewById(R.id.check_mode_light);
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

        tvLight = view.findViewById(R.id.tv_light);
        tvHumid = view.findViewById(R.id.tv_humidity);


        //리사이클러뷰를 담담하는 어댑터 초기화
        waterDataList = new ArrayList<>();
        lightDataList = new ArrayList<>();
        deleteList = new ArrayList<>();
        dataAdapter = new DataAdapter(waterDataList);

        //리사이클러뷰 초기화 진행
        reData.setAdapter(dataAdapter);
        reData.setLayoutManager(new LinearLayoutManager(getContext()));


        tvLight.setText(getData(requireContext(), "light"));
        tvHumid.setText(getData(requireContext(), "humid"));
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
                        checkBoxLight.setVisibility(View.GONE);
                        checkBoxWater.setVisibility(View.VISIBLE);
                        tvValue1.setText(waterValue1);
                        tvValue2.setText(waterValue2);

                        if (edWater1 != null || edWater2 != null) {
                            edValue1.setText(edWater1);
                            edValue2.setText(edWater2);
                        } else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }

                        if (checkBoxWater.isChecked()) {
                            tableCenter.setVisibility(View.VISIBLE);
                            includeView.setVisibility(View.GONE);
                        } else {
                            tableCenter.setVisibility(View.GONE);
                            includeView.setVisibility(View.VISIBLE);
                        }
                        changeText("희망값", "임계범위", "1", "25", "급수시간", "수동급수시간", "");
                        dataAdapter.notifyItemRangeRemoved(0, waterDataList.size());
                        dataAdapter.setDataList(waterDataList);

                        break;
                    case 1:
                        curType = LIGHT;
                        checkBoxWater.setVisibility(View.GONE);
                        checkBoxLight.setVisibility(View.VISIBLE);
                        tvValue1.setText(lightValue1);
                        tvValue2.setText(lightValue2);

                        if (edLight1 != null || edLight2 != null) {
                            edValue1.setText(edLight1);
                            edValue2.setText(edLight2);
                        } else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }

                        if (checkBoxLight.isChecked()) {
                            tableCenter.setVisibility(View.VISIBLE);
                            includeView.setVisibility(View.GONE);
                        } else {
                            tableCenter.setVisibility(View.GONE);
                            includeView.setVisibility(View.VISIBLE);
                        }
                        changeText("조도값", "감지시간", "600", "60", "조명상태", "수동조명시간", "");
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


        checkBoxWater.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                viewModel.setWaterState(isChecked);
               if (isChecked) {
                    includeView.setVisibility(View.GONE);
                    tableCenter.setVisibility(View.VISIBLE);
                } else {
                    includeView.setVisibility(View.VISIBLE);
                    tableCenter.setVisibility(View.GONE);
                }
            }
        });

        checkBoxLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                viewModel.setLightState(isChecked);
                if (isChecked) {
                    includeView.setVisibility(View.GONE);
                    tableCenter.setVisibility(View.VISIBLE);
                } else {
                    includeView.setVisibility(View.VISIBLE);
                    tableCenter.setVisibility(View.GONE);
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


        tvValue1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER)) {
                    waterValue1 = tvValue1.getText().toString();
                } else {
                    lightValue1 = tvValue1.getText().toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("##INFO", "afterTextChanged(): editable = " + editable);
            }
        });

        tvValue2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER)) {
                    waterValue2 = tvValue2.getText().toString();
                } else {
                    lightValue2 = tvValue2.getText().toString();
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


    private String getData(Context context, String key) {

        SharedPreferences prefs = requireContext().getSharedPreferences("data",Context.MODE_PRIVATE);

        String value = prefs.getString(key, "");

        return value;

    }

}