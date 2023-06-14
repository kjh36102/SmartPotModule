package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.CountDownLatch;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Fragment3 extends Fragment {
    public boolean waterState, lightState;
    public AtomicReference<int[]> waterUdsRef = new AtomicReference<>();
    public AtomicReference<String[]> waterStsRef = new AtomicReference<>();
    public AtomicReference<int[]> waterWtsRef = new AtomicReference<>();
    public AtomicReference<int[]> lightUdsRef = new AtomicReference<>();
    public AtomicReference<String[]> lightStsRef = new AtomicReference<>();
    public AtomicReference<int[]> lightLssRef = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch(1);
    public int otReference, hmReference, thReference, ltReference, drReference;

    private TabLayout tabLayout;
    private TableLayout tableCenter, manuLayout;
    private View includeView;
    private TextView tvCheck, tvValue, tvRange, tvValue1, tvValue2, tvValue3, tvValue4, tvValue5, edValue1,edValue2, tvLight, tvHumid;
    private static CheckBox checkBoxWater, checkBoxLight, CheckSetNearestWater, CheckSetNearestLight;
    private String curType = WATER;

    private Button btDelete, btRegister, receiveBt1, receiveBt2, receiveBt3, receiveBt4;;
    private DataAdapter dataAdapter;

    private static ArrayList<DataValue> waterDataList = null;
    private static ArrayList<DataValue> lightDataList = null;
    private ArrayList<DataValue> deleteList;
    private ArrayList<Integer> deleteIndexList;

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
    SharedViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("##INFO", "onCreateView(): fragment3");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_3, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("##INFO", "onViewCreated(): Fragment3");

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        waterState = sharedPreferences.getBoolean("WaterCheckBoxState", false);
        lightState = sharedPreferences.getBoolean("LightCheckBoxState", false);

        receiveBt1 = view.findViewById(R.id.receiveBt1);
        receiveBt2 = view.findViewById(R.id.receiveBt2);
        receiveBt3 = view.findViewById(R.id.receiveBt3);
        receiveBt4 = view.findViewById(R.id.receiveBt4);
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
        CheckSetNearestWater = view.findViewById(R.id.setNearest_water); //최단시간 적용 Water
        CheckSetNearestLight = view.findViewById(R.id.setNearest_light); //최단시간 적용
        checkBoxWater.setChecked(waterState);
        checkBoxLight.setChecked(lightState);

        tvValue3 = includeView.findViewById(R.id.tv_time_to_status);
        btDelete = includeView.findViewById(R.id.bt_delete);
        btRegister = includeView.findViewById(R.id.bt_register);
        reData = includeView.findViewById(R.id.rv_center_data);

        value1 = includeView.findViewById(R.id.ed_value_1);
        value2 = includeView.findViewById(R.id.ed_value_2);
        value3 = includeView.findViewById(R.id.ed_value_3);
        tvValue4 = includeView.findViewById(R.id.tv_a);
        tvValue5 = includeView.findViewById(R.id.tv_b);

        checkBoxWater = view.findViewById(R.id.check_mode_water); //Water 체크박스
        checkBoxLight = view.findViewById(R.id.check_mode_light); //Light 체크박스
        CheckSetNearestWater = view.findViewById(R.id.setNearest_water); //최단시간 적용 Water
        CheckSetNearestLight = view.findViewById(R.id.setNearest_light); //최단시간 적용 Light
        checkBoxWater.setChecked(waterState); //최단시간 적용 Water 체크 여부
        checkBoxLight.setChecked(lightState); //최단시간 적용 Light 체크 여부

        btDelete = includeView.findViewById(R.id.bt_delete);    //삭제버튼
        btRegister = includeView.findViewById(R.id.bt_register);//등록버튼
        reData = includeView.findViewById(R.id.rv_center_data); //item 데이터

        value1 = includeView.findViewById(R.id.ed_value_1); //단위일 edittext
        value2 = includeView.findViewById(R.id.ed_value_2); //지정시간 edittext
        value3 = includeView.findViewById(R.id.ed_value_3); //급수시간 edittext

        edValue1 = includeView.findViewById(R.id.ed_input_1); //수동급수시간 edittext
        edValue2 = includeView.findViewById(R.id.ed_input_2);

        tvLight = view.findViewById(R.id.tv_light); //태양습도 text
        tvHumid = view.findViewById(R.id.tv_humidity); //태양조도 text

        //리사이클러뷰를 담담하는 어댑터 초기화
        waterDataList = new ArrayList<>();
        lightDataList = new ArrayList<>();
        deleteList = new ArrayList<>();
        dataAdapter = new DataAdapter(waterDataList);

        deleteIndexList = new ArrayList<>();

        //리사이클러뷰 초기화 진행
        reData.setAdapter(dataAdapter);
        reData.setLayoutManager(new LinearLayoutManager(getContext()));

        tvLight.setText(getData(requireContext(), "light"));
        tvHumid.setText(getData(requireContext(), "humid"));

        //저장되어 있는 값 가져오기



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            // Tab이 선택되었을 때
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 선택된 Tab의 위치를 가져옴
                int position = tab.getPosition();
                boolean isWaterChecked = sharedPreferences.getBoolean("WaterCheckBoxState", false);
                boolean isLightChecked = sharedPreferences.getBoolean("LightCheckBoxState", false);

                value1.setText("");
                value2.setText("");
                value3.setText("");

                switch (position) {
                    //첫번째 탭 선택 WATER
                    case 0:
                        curType = WATER;
                        checkBoxLight.setVisibility(View.GONE);
                        checkBoxWater.setVisibility(View.VISIBLE);
                        CheckSetNearestWater.setVisibility(View.VISIBLE);
                        CheckSetNearestLight.setVisibility(View.GONE);
                        receiveBt1.setVisibility(View.VISIBLE);
                        receiveBt2.setVisibility(View.GONE);
                        receiveBt3.setVisibility(View.GONE);
                        receiveBt4.setVisibility(View.GONE);

                        tvValue1.setText(waterValue1);
                        tvValue2.setText(waterValue2);
                        if (edWater1 != null || edWater2 != null) {
                            edValue1.setText(edWater1); // 수동급수시간
                            edValue2.setText(edWater2);
                        }
                        else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }

                        // checkBoxWater의 상태에 따라 뷰 표시 여부 설정
                        if (isWaterChecked) {
                            tableCenter.setVisibility(View.VISIBLE);
                            includeView.setVisibility(View.GONE);
                            receiveBt1.setVisibility(View.GONE);
                            receiveBt2.setVisibility(View.VISIBLE);
                            AutoWaterButton.setVisibility(View.VISIBLE);
                            AutoLightButton.setVisibility(View.GONE);
                        } else {
                            tableCenter.setVisibility(View.GONE);
                            includeView.setVisibility(View.VISIBLE);
                            receiveBt1.setVisibility(View.VISIBLE);
                            receiveBt2.setVisibility(View.GONE);
                        }
                        changeText("희망값", "임계범위", "1", "25", "급수시간", "수동급수시간", "");
                        dataAdapter.notifyItemRangeRemoved(0, waterDataList.size());
                        dataAdapter.setDataList(waterDataList);

                        break;
                    case 1:
                        // 두 번째 탭 선택 LIGHT
                        curType = LIGHT;
                        checkBoxWater.setVisibility(View.GONE);
                        checkBoxLight.setVisibility(View.VISIBLE);
                        CheckSetNearestWater.setVisibility(View.GONE);
                        CheckSetNearestLight.setVisibility(View.VISIBLE);
                        receiveBt1.setVisibility(View.GONE);
                        receiveBt2.setVisibility(View.GONE);
                        receiveBt3.setVisibility(View.VISIBLE);
                        receiveBt4.setVisibility(View.GONE);
                        tvValue1.setText(lightValue1);
                        tvValue2.setText(lightValue2);
                        if (edLight1 != null || edLight2 != null) {
                            edValue1.setText(edLight1); // 수동조명시간
                            edValue2.setText(edLight2);
                        }
                        else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }

                        // checkBoxLight의 상태에 따라 뷰 표시 여부 설정
                        if (isLightChecked) {
                            tableCenter.setVisibility(View.VISIBLE);
                            includeView.setVisibility(View.GONE);
                            receiveBt3.setVisibility(View.GONE);
                            receiveBt4.setVisibility(View.VISIBLE);
                            AutoWaterButton.setVisibility(View.GONE);
                            AutoLightButton.setVisibility(View.VISIBLE);
                        }
                        else {
                            tableCenter.setVisibility(View.GONE);
                            includeView.setVisibility(View.VISIBLE);
                            receiveBt3.setVisibility(View.VISIBLE);
                            receiveBt4.setVisibility(View.GONE);
                        }
                        changeText("조도값", "감지시간", "600", "60", "조명상태", "", "");
                        dataAdapter.setDataList(lightDataList);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Tab이 선택 해제될 때 호출되는 메서드

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 이미 선택된 Tab이 다시 선택될 때 호출되는 메서드
         
            }
        });

        manuWaterRegister.setOnClickListener(v -> {
            String value1 = edValue1.getText().toString();
            boolean isNotNullOfManuValue = isNotNullOfManuValue(value1);

            if (isNotNullOfManuValue) {
                Thread thread5 = new Thread(() -> {
                    try {
                        URL url = new URL(popup.url +
                                "manageAutoSet?ot=" + value1
                        );
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

                        getActivity().runOnUiThread(() -> {
                            if (parsed[0].equals("ok")) {
                                Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                checkConnectAndRun(thread5);
            }
        });

        checkBoxWater.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("WaterCheckBoxState", isChecked);
                editor.apply();
                viewModel.setWaterState(isChecked);

                if (isChecked) {
                    includeView.setVisibility(View.GONE);
                    tableCenter.setVisibility(View.VISIBLE);
                    receiveBt1.setVisibility((View.GONE));
                    receiveBt2.setVisibility(View.VISIBLE);
                } else {
                    includeView.setVisibility(View.VISIBLE);
                    tableCenter.setVisibility(View.GONE);
                    receiveBt1.setVisibility((View.VISIBLE));
                    receiveBt2.setVisibility(View.GONE);
                }
            }
        });

        checkBoxLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("LightCheckBoxState", isChecked);
                editor.apply();
                viewModel.setLightState(isChecked);

                if (isChecked) {
                    includeView.setVisibility(View.GONE);
                    tableCenter.setVisibility(View.VISIBLE);
                    receiveBt3.setVisibility((View.GONE));
                    receiveBt4.setVisibility(View.VISIBLE);
                } else {
                    includeView.setVisibility(View.VISIBLE);
                    tableCenter.setVisibility(View.GONE);
                    receiveBt3.setVisibility((View.VISIBLE));
                    receiveBt4.setVisibility(View.GONE);
                }
            }
        });

        //등록부분
        btRegister.setOnClickListener(v -> {
            String dateData = value1.getText().toString();
            String timeData = value2.getText().toString();
            String valueData = value3.getText().toString();

            if (Objects.equals(curType, WATER)) {
                //현재 타입이 WATER인 경우
                //최대 6개까지만 입력 가능
                if (waterDataList.size() < 7) {
                    DataValue dataValue = new DataValue();

                    // 입력된 값으로 DataValue 객체 생성
                    boolean isNearest = CheckSetNearestWater.isChecked();
                    boolean isNotNullOfValue = isNotNullOfValue(dateData, timeData, valueData);
                    if (isNotNullOfValue) {
                        dataValue.setDate(dateData); //단위일
                        dataValue.setTime(timeData); //지정시간
                        dataValue.setValue(valueData); //급수시간

                        Thread thread1 = new Thread(() -> {
                            try {
                                System.out.println("is Nearst : " + isNearest);
                                URL url = new URL(popup.url +
                                        "manageAdd?table=manage_water&ud=" + dateData +
                                        "&st=" + timeData +
                                        "&ls=&wt=" + valueData +
                                        "&setNearest=" + isNearest
                                );
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

                                getActivity().runOnUiThread(() -> {
                                    if (parsed[0].equals("ok")) {
                                        // waterDataList에 DataValue 객체 추가
                                        waterDataList.add(dataValue);
                                        dataAdapter.setDataList(waterDataList);
                                        Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();

                                        clearValue();
                                    } else {
                                        switch (parsed[1]) {
                                            case "0":
                                                Toast.makeText(getContext(), "레코드 꽉참", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "1":
                                                Toast.makeText(getContext(), "단위일이 0 이하임", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "2":
                                                Toast.makeText(getContext(), "시간 형식이 맞지 않음", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "3":
                                                Toast.makeText(getContext(), "Insert sql 실패", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "4":
                                                Toast.makeText(getContext(), "쿼리 파싱 오류", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        checkConnectAndRun(thread1);
                    }
                }
            } else {
                //현재 타입이 WATER가 아닌 경우
                //최대 6개까지만 입력 가능
                if (lightDataList.size() < 7) {
                    DataValue dataValue = new DataValue();

                    // 입력된 값으로 DataValue 객체 생성
                    boolean isNearest = CheckSetNearestWater.isChecked();
                    boolean isNotNullOfValue = isNotNullOfValue(dateData, timeData, valueData);
                    if (isNotNullOfValue) {
                        dataValue.setDate(dateData); //단위일
                        dataValue.setTime(timeData); //지정시간
                        dataValue.setValue(valueData); //급수시간

                        Thread thread2 = new Thread(() -> {
                            try {
                                System.out.println("is Nearst : " + isNearest);
                                URL url = new URL(popup.url +
                                        "manageAdd?table=manage_light&ud=" + dateData +
                                        "&st=" + timeData +
                                        "&ls=" + valueData +
                                        "&wt=&setNearest=" + isNearest
                                );
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

                                getActivity().runOnUiThread(() -> {
                                    if (parsed[0].equals("ok")) {
                                        // lightDataList에 DataValue 객체 추가
                                        lightDataList.add(dataValue);
                                        dataAdapter.setDataList(lightDataList);
                                        Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();

                                        clearValue();
                                    } else {
                                        switch (parsed[1]) {
                                            case "0":
                                                Toast.makeText(getContext(), "레코드 꽉참", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "1":
                                                Toast.makeText(getContext(), "단위일이 0 이하임", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "2":
                                                Toast.makeText(getContext(), "시간 형식이 맞지 않음", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "3":
                                                Toast.makeText(getContext(), "Insert sql 실패", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "4":
                                                Toast.makeText(getContext(), "쿼리 파싱 오류", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        checkConnectAndRun(thread2);
                    }
                }

            }
        });

        //삭제버튼 클릭 이벤트 부분
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.equals(curType, WATER)) {
                    //급수모드
                    if (waterDataList.size() > 0) {
                        boolean isNotNullOfDelete = isNotNullOfDelete();

                        if (isNotNullOfDelete) {
                            Thread thread3 = new Thread(() -> {
                                try {
                                    StringBuilder builder = new StringBuilder();
                                    builder.append("[");
                                    for (int i = 0; i < deleteIndexList.size(); i++) {
                                        builder.append(deleteIndexList.get(i));
                                        if (deleteIndexList.size() > 1 && i != deleteIndexList.size() - 1) {
                                            builder.append(", ");
                                        }
                                    }
                                    builder.append("]");
                                    URL url = new URL(popup.url +
                                            "manageDelete?table=manage_water&id=" + builder
                                    );
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

                                    getActivity().runOnUiThread(() -> {
                                        if (parsed[0].equals("ok")) {
                                            dataAdapter.removeData(deleteList);
                                            deleteList.clear();
                                            deleteIndexList.clear();

                                            Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                        } else {
                                            switch (parsed[1]) {
                                                case "0":
                                                    Toast.makeText(getContext(), "테이블이 비어있음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "1":
                                                    Toast.makeText(getContext(), "쿼리 id 리스트 형식 맞지않음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "2":
                                                    Toast.makeText(getContext(), "쿼리 id 리스트 길이가 레코드 최대수를 넘음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "3":
                                                    Toast.makeText(getContext(), "id 값들 중 0보다 작은값이 있음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "4":
                                                    Toast.makeText(getContext(), "delete sql 실패", Toast.LENGTH_SHORT).show();
                                                    break;
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            checkConnectAndRun(thread3);
                        }
                    }
                } else {
                    //조명모드
                    if (lightDataList.size() > 0) {
                        boolean isNotNullOfDelete = isNotNullOfDelete();

                        if (isNotNullOfDelete) {
                            Thread thread4 = new Thread(() -> {
                                try {
                                    StringBuilder builder = new StringBuilder();
                                    builder.append("[");
                                    for (int i = 0; i < deleteIndexList.size(); i++) {
                                        builder.append(deleteIndexList.get(i));
                                        if (deleteIndexList.size() > 1 && i != deleteIndexList.size() - 1) {
                                            builder.append(", ");
                                        }
                                    }
                                    builder.append("]");
                                    URL url = new URL(popup.url +
                                            "manageDelete?table=manage_light&id=" + builder
                                    );
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

                                    getActivity().runOnUiThread(() -> {
                                        if (parsed[0].equals("ok")) {
                                            dataAdapter.removeData(deleteList);
                                            deleteList.clear();
                                            deleteIndexList.clear();


                                            Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                        } else {
                                            switch (parsed[1]) {
                                                case "0":
                                                    Toast.makeText(getContext(), "테이블이 비어있음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "1":
                                                    Toast.makeText(getContext(), "쿼리 id 리스트 형식 맞지않음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "2":
                                                    Toast.makeText(getContext(), "쿼리 id 리스트 길이가 레코드 최대수를 넘음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "3":
                                                    Toast.makeText(getContext(), "id 값들 중 0보다 작은값이 있음", Toast.LENGTH_SHORT).show();
                                                    break;
                                                case "4":
                                                    Toast.makeText(getContext(), "delete sql 실패", Toast.LENGTH_SHORT).show();
                                                    break;
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            checkConnectAndRun(thread4);
                        }
                    }
                }
            }
        });

        dataAdapter.setItemClickCallback(new DataAdapter.ItemClickCallback() {
            @Override
            public void onClick(int position) {
                // 데이터 어댑터의 아이템 클릭 콜백 설정
                ArrayList<DataValue> list = new ArrayList<>();
                // 현재 타입에 따라 list 변수에 해당하는 데이터 리스트 할당
                if (curType.equals(WATER)){
                    list = waterDataList;
                }
                else {
                    list = lightDataList;
                }

                if (deleteList.contains(list.get(position))) {
                    // deleteList에 클릭된 데이터가 이미 포함되어 있는 경우
                    Log.i("##INFO", "onClick(): remove");
                    deleteList.remove(list.get(position));
                    deleteIndexList.remove(position);
                }
                else {
                    // deleteList에 클릭된 데이터가 포함되어 있지 않은 경우
                    Log.i("##INFO", "onClick(): add = " + list.get(position).date);
                    deleteList.add(list.get(position));
                    deleteIndexList.add(position);
                }
            }
        });

        edValue1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // EditText의 텍스트가 변경되었을 때 호출되는 메서드
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER)) {
                    // 현재 타입이 WATER인 경우
                    // edValue1의 텍스트 값을 edWater1에 저장
                    edWater1 = edValue1.getText().toString();
                }
                else {
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
                }
                else {
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
                // TextView의 텍스트가 변경되었을 때 호출되는 메서드
                if (curType.equals(WATER)) {
                    // 현재 타입이 WATER인 경우
                    // tvValue1의 텍스트 값을 waterValue1에 저장
                    waterValue1 = tvValue1.getText().toString();
                }
                else {
                    // 현재 타입이 WATER가 아닌 경우
                    // tvValue1의 텍스트 값을 lightValue1에 저장
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
                }
                else {
                    lightValue2 = tvValue2.getText().toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("##INFO", "afterTextChanged(): editable = " + editable);
            }
        });
        receiveBt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //데이터 값 받는 버튼
                manuWaterData(new Runnable() {
                    @Override
                    public void run() {
                        manuWaterArray();
                    }
                });
//                manuWaterData();
//                manuWaterArray();
            }
        });
        receiveBt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //데이터 값 받는 버튼
                autoWaterData();
            }
        });
        receiveBt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //데이터 값 받는 버튼
//                manuLightData();
                manuLightArray();
            }
        });
        receiveBt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //데이터 값 받는 버튼
                autoLightData();
            }
        });

    }

    public void manuWaterData(final Runnable callback){
        new Thread(new Runnable() {
            @Override
            public void run() { //http통신으로 받아옴
                System.out.println("manuWaterData시작 -----------------------");
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL("http://cofon.xyz:9090/getTableData?name=manage_auto");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(30000); // 15 seconds
                    urlConnection.setReadTimeout(15000); // 15 seconds
                    urlConnection.setRequestProperty("Connection", "close");

                    urlConnection.connect();
                    // HTTP 상태 코드 확인
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.out.println("HTTP error code: " + responseCode);
                        return;
                    }

                    InputStream inputStream = urlConnection.getInputStream();

                    if (inputStream == null) {
                        System.out.println("inputStream ㅜull");
                        return;
                    }

                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder output = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);

                    }
                    reader.close();
                    inputStream.close();
                    urlConnection.disconnect();
                    if (output.length() == 0) {
                        System.out.println("받은 값 길이가 0");
                        return;
                    }
                    String[] parts = output.toString().split("\\|");
                    if (parts[0].equals("err")) {
                        System.out.println("error받음");
                    }

                    JSONArray jsonArray = new JSONArray(parts[2]);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    System.out.println("첫번째 jsonObject: " + jsonObject);
                    //sample data test
                    otReference = jsonObject.getInt("ot");

                    System.out.println("ot: " + otReference);


                } catch (IOException ioException) {
                    System.out.println("IO error: " + ioException.getMessage());
                    ioException.printStackTrace();
                } catch (JSONException jsonException) {
                    System.out.println("JSON parsing error");
                    jsonException.printStackTrace();
                } catch (Exception e) {
                    System.out.println("Exception error");
                    e.printStackTrace();
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }

                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (callback != null) {
                        new Thread(callback).start();
                    }
                }
            }
        }).start();
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

    private boolean isNotNullOfValue(String value1, String value2, String value3) {
        return !value1.equals("") && !value2.equals("") && !value3.equals("");
    }

    private boolean isNotNullOfDelete() {
        return !deleteIndexList.isEmpty();
    }

    private void clearValue() {
        value1.setText("");
        value2.setText("");
        value3.setText("");
    }

    private void deleteWaterRecord(String data) {
        if (!data.equals("")) {
            deleteList.add(waterDataList.get(Integer.parseInt(data)));
        }
    }

    private void deleteLightRecord(String data) {
        if (!data.equals("")) {
            deleteList.add(lightDataList.get(Integer.parseInt(data)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        tvHumid.setText(humid);
        tvLight.setText(light);
        
//        //배열을 저장해야함

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        waterState = sharedPreferences.getBoolean("WaterCheckBoxState", false);
        lightState = sharedPreferences.getBoolean("LightCheckBoxState", false);


        checkBoxWater.setChecked(waterState);
        checkBoxLight.setChecked(lightState);

        JSONArray waterUdJsonArray = null;
        JSONArray waterStJsonArray = null;
        JSONArray waterWtJsonArray = null;
        JSONArray lightUdJsonArray = null;
        JSONArray lightStJsonArray = null;
        JSONArray lightLsJsonArray = null;

//        try {
//
//            waterUdJsonArray = new JSONArray(sharedPreferences.getString("wateruds", "[]"));
//            waterStJsonArray = new JSONArray(sharedPreferences.getString("watersts", "[]"));
//            waterWtJsonArray = new JSONArray(sharedPreferences.getString("waterwts", "[]"));
//            lightUdJsonArray = new JSONArray(sharedPreferences.getString("lightuds", "[]"));
//            lightStJsonArray = new JSONArray(sharedPreferences.getString("lightsts", "[]"));
//            lightLsJsonArray = new JSONArray(sharedPreferences.getString("lightlss", "[]"));
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //ui적용
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        // Tab이 선택되었을 때
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                // 선택된 Tab의 위치를 가져옴
//                int position = tab.getPosition();
//                value1.setText("");
//                value2.setText("");
//                value3.setText("");
//
////                switch (position) {
////                    case 0:
////                        curType = WATER;
////                        checkBoxLight.setVisibility(View.GONE);
////                        checkBoxWater.setVisibility(View.VISIBLE);
////                        CheckSetNearestWater.setVisibility(View.VISIBLE);
////                        CheckSetNearestLight.setVisibility(View.GONE);
////                        receiveBt1.setVisibility(View.VISIBLE);
////                        receiveBt2.setVisibility(View.GONE);
////                        receiveBt3.setVisibility(View.GONE);
////                        receiveBt4.setVisibility(View.GONE);
////
////                        tvValue1.setText(waterValue1);
////                        tvValue2.setText(waterValue2);
////
////                        if (edWater1 != null || edWater2 != null) {
////                            edValue1.setText(edWater1);
////                            edValue2.setText(edWater2);
////                        } else {
////                            edValue1.setText("");
////                            edValue2.setText("");
////                        }
////
////                        if (checkBoxWater.isChecked()) {
////                            tableCenter.setVisibility(View.VISIBLE);
////                            includeView.setVisibility(View.GONE);
////                        } else {
////                            tableCenter.setVisibility(View.GONE);
////                            includeView.setVisibility(View.VISIBLE);
////                        }
////                        changeText("희망값", "임계범위", "1", "25", "급수시간", "수동급수시간", "");
////                        dataAdapter.notifyItemRangeRemoved(0, waterDataList.size());
////                        dataAdapter.setDataList(waterDataList);
////
////                        break;
////                    case 1:
////                        curType = LIGHT;
////                        checkBoxWater.setVisibility(View.GONE);
////                        checkBoxLight.setVisibility(View.VISIBLE);
////                        CheckSetNearestWater.setVisibility(View.GONE);
////                        CheckSetNearestLight.setVisibility(View.VISIBLE);
////                        receiveBt1.setVisibility(View.GONE);
////                        receiveBt2.setVisibility(View.GONE);
////                        receiveBt3.setVisibility(View.VISIBLE);
////                        receiveBt4.setVisibility(View.GONE);
////                        tvValue1.setText(lightValue1);
////                        tvValue2.setText(lightValue2);
////
////                        if (edLight1 != null || edLight2 != null) {
////                            edValue1.setText(edLight1);
////                            edValue2.setText(edLight2);
////                        } else {
////                            edValue1.setText("");
////                            edValue2.setText("");
////                        }
////
////                        if (checkBoxLight.isChecked()) {
////                            tableCenter.setVisibility(View.VISIBLE);
////                            includeView.setVisibility(View.GONE);
////                        } else {
////                            tableCenter.setVisibility(View.GONE);
////                            includeView.setVisibility(View.VISIBLE);
////                        }
////                        changeText("조도값", "감지시간", "600", "60", "조명상태", "수동조명시간", "");
////                        dataAdapter.setDataList(lightDataList);
////                        break;
////
////                }
////            }
////
////            @Override
////            public void onTabUnselected(TabLayout.Tab tab) {
////
////            }
////
////            @Override
////            public void onTabReselected(TabLayout.Tab tab) {
////
////            }
////
////        });
    }
}