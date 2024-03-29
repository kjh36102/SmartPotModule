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

import android.preference.PreferenceManager;
import android.widget.Toast;

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
    private TextView tvCheck, tvValue, tvRange, tvValue1, tvValue2, tvValue3, tvValue4, tvValue5, edValue1, edValue2, tvLight, tvHumid;
    private static CheckBox checkBoxWater, checkBoxLight, CheckSetNearestWater, CheckSetNearestLight;
    private String curType = WATER;

    private Button btDelete, btRegister, AutoWaterButton, AutoLightButton, manuWaterRegister;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("##INFO", "onCreateView(): fragment3");
        return inflater.inflate(R.layout.fragment_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("##INFO", "onViewCreated(): Fragment3");
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean waterState = sharedPreferences.getBoolean("WaterCheckBoxState", false);
        boolean lightState = sharedPreferences.getBoolean("LightCheckBoxState", false);

        AutoWaterButton = view.findViewById(R.id.autoWaterRegister); 
        AutoLightButton = view.findViewById(R.id.autoLightRegister); 
        manuWaterRegister = view.findViewById((R.id.manuWaterRegister)); 

        tvCheck = view.findViewById(R.id.tv_check_name);    
        tabLayout = view.findViewById(R.id.tab_mode);
        tableCenter = view.findViewById(R.id.tab_layout_center); 
        manuLayout = view.findViewById(R.id.tab_layout_center_checked); 

        tvValue = view.findViewById(R.id.tv_want_value);    
        tvRange = view.findViewById(R.id.tv_range);         
        includeView = view.findViewById(R.id.in_uncheck);   
        tvValue1 = view.findViewById(R.id.tv_value_1);      
        tvValue2 = view.findViewById(R.id.tv_value_2);      
        tvValue3 = includeView.findViewById(R.id.tv_time_to_status); 
        tvValue4 = includeView.findViewById(R.id.tv_a);     
        tvValue5 = includeView.findViewById(R.id.tv_b);

        checkBoxWater = view.findViewById(R.id.check_mode_water); 
        checkBoxLight = view.findViewById(R.id.check_mode_light); 
        CheckSetNearestWater = view.findViewById(R.id.setNearest_water); 
        CheckSetNearestLight = view.findViewById(R.id.setNearest_light); 
        checkBoxWater.setChecked(waterState); 
        checkBoxLight.setChecked(lightState); 

        btDelete = includeView.findViewById(R.id.bt_delete);    
        btRegister = includeView.findViewById(R.id.bt_register);
        reData = includeView.findViewById(R.id.rv_center_data); 

        value1 = includeView.findViewById(R.id.ed_value_1); 
        value2 = includeView.findViewById(R.id.ed_value_2); 
        value3 = includeView.findViewById(R.id.ed_value_3); 

        edValue1 = includeView.findViewById(R.id.ed_input_1); 
        edValue2 = includeView.findViewById(R.id.ed_input_2);

        tvLight = view.findViewById(R.id.tv_light); 
        tvHumid = view.findViewById(R.id.tv_humidity); 

        waterDataList = new ArrayList<>();
        lightDataList = new ArrayList<>();
        deleteList = new ArrayList<>();
        dataAdapter = new DataAdapter(waterDataList);
        deleteIndexList = new ArrayList<>();

        reData.setAdapter(dataAdapter); 
        reData.setLayoutManager(new LinearLayoutManager(getContext()));

        tvLight.setText(getData(requireContext(), "light"));
        tvHumid.setText(getData(requireContext(), "humid"));

        initView(waterState);
        manuWaterData(() -> manuWaterArray());
        autoWaterData(); 

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                
                int position = tab.getPosition();
                boolean isWaterChecked = sharedPreferences.getBoolean("WaterCheckBoxState", false);
                boolean isLightChecked = sharedPreferences.getBoolean("LightCheckBoxState", false);

                value1.setText("");
                value2.setText("");
                value3.setText("");

                switch (position) { 
                    case 0:
                        manuWaterData(() -> manuWaterArray());
                        autoWaterData(); 

                        curType = WATER;
                        checkBoxLight.setVisibility(View.GONE);
                        checkBoxWater.setVisibility(View.VISIBLE);
                        CheckSetNearestWater.setVisibility(View.VISIBLE);
                        CheckSetNearestLight.setVisibility(View.GONE);
                        manuLayout.setVisibility(View.VISIBLE);

                        tvValue1.setText(waterValue1); 
                        tvValue2.setText(waterValue2); 

                        
                        if (edWater1 != null || edWater2 != null) {
                            edValue1.setText(edWater1); 
                            edValue2.setText(edWater2);
                        }
                        else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }
                        if (isWaterChecked) {
                            tableCenter.setVisibility(View.VISIBLE);
                            includeView.setVisibility(View.GONE);
                            AutoWaterButton.setVisibility(View.VISIBLE);
                            AutoLightButton.setVisibility(View.GONE);
                            edValue2.setEnabled(true);
                        } else {
                            tableCenter.setVisibility(View.GONE);
                            includeView.setVisibility(View.VISIBLE);
                            edValue2.setEnabled(false);
                        }
                        changeText("희망값", "임계범위", "1", "25", "급수시간", "수동급수시간", "");
                        dataAdapter.notifyItemRangeRemoved(0, waterDataList.size());
                        dataAdapter.setDataList(waterDataList);
                        break;
                    case 1:  
                        manuLightArray();
                        autoLightData();

                        curType = LIGHT;
                        checkBoxWater.setVisibility(View.GONE);
                        checkBoxLight.setVisibility(View.VISIBLE);
                        CheckSetNearestWater.setVisibility(View.GONE);
                        CheckSetNearestLight.setVisibility(View.VISIBLE);
                        manuLayout.setVisibility(View.GONE);

                        tvValue1.setText(lightValue1); 
                        tvValue2.setText(lightValue2); 

                        if (edLight1 != null || edLight2 != null) {
                            edValue1.setText(edLight1); 
                            edValue2.setText(edLight2);
                        }
                        else {
                            edValue1.setText("");
                            edValue2.setText("");
                        }
                        if (isLightChecked) {
                            tableCenter.setVisibility(View.VISIBLE);
                            includeView.setVisibility(View.GONE);
                            AutoWaterButton.setVisibility(View.GONE);
                            AutoLightButton.setVisibility(View.VISIBLE);
                        }
                        else {
                            tableCenter.setVisibility(View.GONE);
                            includeView.setVisibility(View.VISIBLE);
                        }
                        changeText("조도값", "감지시간", "600", "60", "조명상태", "", "");
                        dataAdapter.setDataList(lightDataList);
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {  }
        });

        AutoWaterButton.setOnClickListener(v -> {
            String value1 = tvValue1.getText().toString();
            String value2 = tvValue2.getText().toString();
            boolean isNotNullOfAutoValue = isNotNullOfAutoValue(value1, value2);
            if (isNotNullOfAutoValue) {
                Thread thread = new Thread(() -> {
                    try {
                        URL url = new URL(popup.url +"manageAutoSet?hm=" + value1 + "&th=" + value2);
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
                            if (parsed[0].equals("ok"))
                                Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                checkConnectAndRun(thread);
            }
        });

        AutoLightButton.setOnClickListener(v -> {
            String value1 = tvValue1.getText().toString();
            String value2 = tvValue2.getText().toString();
            boolean isNotNullOfAutoValue = isNotNullOfAutoValue(value1, value2);

            if (isNotNullOfAutoValue) {
                Thread thread = new Thread(() -> {
                    try {
                        URL url = new URL(popup.url +"manageAutoSet?lt=" + value1 + "&dr=" + value2);
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
                            if (parsed[0].equals("ok"))
                                Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                checkConnectAndRun(thread);
            }
        });

        manuWaterRegister.setOnClickListener(v -> {
            String value1 = edValue1.getText().toString();
            boolean isNotNullOfManuValue = isNotNullOfManuValue(value1);

            if (isNotNullOfManuValue) {
                Thread thread = new Thread(() -> {
                    try {
                        URL url = new URL(popup.url + "manageAutoSet?ot=" + value1);
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
                            if (parsed[0].equals("ok"))
                                Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                checkConnectAndRun(thread);
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
                    AutoWaterButton.setVisibility(View.VISIBLE);
                    AutoLightButton.setVisibility(View. GONE);
                    edValue2.setEnabled(true);

                    new Thread(() -> {
                        try {
                            URL url = new URL(popup.url +"plantManageSet?w_auto=1");
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
                                if (parsed[0].equals("ok"))
                                    Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    includeView.setVisibility(View.VISIBLE);
                    tableCenter.setVisibility(View.GONE);
                    edValue2.setEnabled(false);

                    new Thread(() -> {
                        try {
                            URL url = new URL(popup.url +"plantManageSet?w_auto=0");
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
                                if (parsed[0].equals("ok"))
                                    Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();

                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
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
                    AutoWaterButton.setVisibility(View.GONE);
                    AutoLightButton.setVisibility(View.VISIBLE);

                    new Thread(() -> {
                        try {
                            URL url = new URL(popup.url +"plantManageSet?l_auto=1");
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
                                if (parsed[0].equals("ok"))
                                    Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    includeView.setVisibility(View.VISIBLE);
                    tableCenter.setVisibility(View.GONE);

                    new Thread(() -> {
                        try {
                            URL url = new URL(popup.url +"plantManageSet?l_auto=0");
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
                                if (parsed[0].equals("ok"))
                                    Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getContext(), "업데이트 sql 실패", Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });

        
        btRegister.setOnClickListener(v -> {
            String dateData = value1.getText().toString();
            String timeData = value2.getText().toString();
            String valueData = value3.getText().toString();

            if (Objects.equals(curType, WATER)) {
                
                
                if (waterDataList.size() < 7) {
                    DataValue dataValue = new DataValue();
                    
                    boolean isNearest = CheckSetNearestWater.isChecked();
                    boolean isNotNullOfValue = isNotNullOfValue(dateData, timeData, valueData);
                    if (isNotNullOfValue) {
                        dataValue.setDate(dateData); 
                        dataValue.setTime(timeData); 
                        dataValue.setValue(valueData); 
                        Thread thread = new Thread(() -> {
                            try {
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
                                        
                                        waterDataList.add(dataValue);
                                        dataAdapter.setDataList(waterDataList);
                                        Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                        clearValue();
                                    } else
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
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        checkConnectAndRun(thread);
                    }
                }
            } else {
                
                
                if (lightDataList.size() < 7) {
                    DataValue dataValue = new DataValue();
                    
                    boolean isNearest = CheckSetNearestLight.isChecked();
                    boolean isNotNullOfValue = isNotNullOfValue(dateData, timeData, valueData);
                    if (isNotNullOfValue) {
                        dataValue.setDate(dateData); 
                        dataValue.setTime(timeData); 
                        dataValue.setValue(valueData); 

                        Thread thread = new Thread(() -> {
                            try {
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
                                        lightDataList.add(dataValue);
                                        dataAdapter.setDataList(lightDataList);
                                        Toast.makeText(getContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                                        clearValue();
                                    } else
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
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        checkConnectAndRun(thread);
                    }
                }

            }
        });
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.equals(curType, WATER)) {
                    if (waterDataList.size() > 0) {
                        boolean isNotNullOfDelete = isNotNullOfDelete();
                        if (isNotNullOfDelete) {
                            Thread thread = new Thread(() -> {
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
                                    URL url = new URL(popup.url +"manageDelete?table=manage_water&id=" + builder);
                                    
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
                                        } else
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
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            checkConnectAndRun(thread);
                        }
                    }
                } else {
                    
                    if (lightDataList.size() > 0) {
                        boolean isNotNullOfDelete = isNotNullOfDelete();

                        if (isNotNullOfDelete) {
                            Thread thread = new Thread(() -> {
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
                                    URL url = new URL(popup.url + "manageDelete?table=manage_light&id=" + builder);
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
                                        } else
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
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            checkConnectAndRun(thread);
                        }
                    }
                }
            }
        });

        dataAdapter.setItemClickCallback(new DataAdapter.ItemClickCallback() {
            @Override
            public void onClick(int position) {
                ArrayList<DataValue> list = new ArrayList<>();
                if (curType.equals(WATER))
                    list = waterDataList;
                else
                    list = lightDataList;
                if (deleteList.contains(list.get(position))) { 
                    Log.i("##INFO", "onClick(): remove");
                    deleteList.remove(list.get(position));
                    deleteIndexList.remove(list.get(position));
                }
                else {                    
                    Log.i("##INFO", "onClick(): add = " + list.get(position).date);
                    deleteList.add(list.get(position));
                    deleteIndexList.add(position);
                }
            }
        });

        edValue1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER))  
                    edWater1 = edValue1.getText().toString();
                else
                    edLight1 = edValue1.getText().toString();
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        edValue2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER))
                    edWater2 = edValue2.getText().toString();
                else
                    edLight2 = edValue2.getText().toString();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("##INFO", "afterTextChanged(): editable = " + editable);
            }
        });

        tvValue1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER))  
                    waterValue1 = tvValue1.getText().toString(); 
                else  
                    lightValue1 = tvValue1.getText().toString();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("##INFO", "afterTextChanged(): editable = " + editable);
            }
        });

        tvValue2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (curType.equals(WATER))
                    waterValue2 = tvValue2.getText().toString();
                else
                    lightValue2 = tvValue2.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i("##INFO", "afterTextChanged(): editable = " + editable);
            }
        });
    }

    public void manuWaterData(final Runnable callback){
        new Thread(new Runnable() {
            @Override
            public void run() { 
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;
                boolean executeCallback = true;
                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL(popup.url +"getTableData?name=manage_auto");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(30000); 
                    urlConnection.setReadTimeout(15000); 
                    urlConnection.setRequestProperty("Connection", "close");
                    urlConnection.connect();
                    responseCode = urlConnection.getResponseCode(); 
                    if (responseCode != HttpURLConnection.HTTP_OK)
                        return;
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null)
                        return;
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder output = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                    reader.close();
                    inputStream.close();
                    urlConnection.disconnect();
                    if (output.length() == 0)
                        return;
                    String[] parts = output.toString().split("\\|");

                    JSONArray jsonArray = new JSONArray(parts[2]);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    otReference = jsonObject.getInt("ot");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    responseCode = 0;
                    if (getContext() != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "다시 시도", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    executeCallback = false;
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (callback != null && executeCallback == true)
                        new Thread(callback).start();
                }
            }
        }).start();
    }
    public void manuWaterArray() {
        new Thread(new Runnable(){
            @Override
            public void run() { 
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;

                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL(popup.url + "getTableData?name=manage_water");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); 
                    urlConnection.setReadTimeout(15000); 
                    urlConnection.setRequestProperty("Connection", "close");
                    urlConnection.connect();
                    responseCode = urlConnection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK)
                        return;
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null)
                        return;
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder output = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                    reader.close();
                    inputStream.close();
                    urlConnection.disconnect();

                    if (output.length() == 0)
                        return;
                    String[] parts = output.toString().split("\\|");
                    JSONArray jsonArray = new JSONArray(parts[2]);
                    int[] uds = new int[jsonArray.length()];
                    String[] sts = new String[jsonArray.length()];
                    int[] wts = new int[jsonArray.length()];

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        uds[i] = jsonObject.getInt("ud");
                        sts[i] = jsonObject.getString("st");
                        wts[i] = jsonObject.getInt("wt");
                    }
                    waterUdsRef.set(uds);
                    waterStsRef.set(sts);
                    waterWtsRef.set(wts);

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    responseCode = 0;
                    if (getContext() != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "다시 시도", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException jsonException){
                    jsonException.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(responseCode == HttpURLConnection.HTTP_OK){
                    int[] uds = waterUdsRef.get();
                    String[] sts = waterStsRef.get();
                    int[] wts = waterWtsRef.get();
                    JSONArray udJsonArray = new JSONArray();
                    if (uds != null) {
                        for (int ud : uds) {
                            udJsonArray.put(ud);
                        }
                    }
                    JSONArray stJsonArray = new JSONArray();
                    if (sts != null) {
                        for (String st : sts) {
                            stJsonArray.put(st);
                        }
                    }
                    JSONArray wtJsonArray = new JSONArray();
                    if (wts != null) {
                        for (int wt : wts) {
                            wtJsonArray.put(wt);
                        }
                    }
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("otReference", otReference);
                    editor.apply();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waterDataList.clear();
                            edWater1 = Integer.toString(otReference);
                            if (!edWater1.equals(""))
                                edValue1.setText(edWater1);
                            for (int i = 0; i < udJsonArray.length(); i++) {
                                try {
                                    DataValue dataValue = new DataValue();
                                    dataValue.setDate(udJsonArray.get(i).toString());
                                    dataValue.setTime(stJsonArray.get(i).toString());
                                    dataValue.setValue(wtJsonArray.get(i).toString());
                                    waterDataList.add(dataValue);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            dataAdapter.setDataList(waterDataList);
                            dataAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }
    public void autoWaterData(){
        new Thread(new Runnable() {
            @Override
            public void run() { 
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;

                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL(popup.url +"getTableData?name=manage_auto");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); 
                    urlConnection.setReadTimeout(15000); 
                    urlConnection.setRequestProperty("Connection", "close");
                    urlConnection.connect();

                    responseCode = urlConnection.getResponseCode(); 
                    if (responseCode != HttpURLConnection.HTTP_OK)
                        return;
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null)
                        return;
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                    reader.close();
                    inputStream.close();
                    urlConnection.disconnect();
                    if (output.length() == 0)
                        return;

                    String[] parts = output.toString().split("\\|");
                    JSONArray jsonArray = new JSONArray(parts[2]);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    hmReference = jsonObject.getInt("hm");
                    thReference = jsonObject.getInt("th");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    responseCode = 0;
                    if (getContext() != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "다시 시도", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }catch (JSONException jsonException){
                    jsonException.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("hmReference", hmReference);
                        editor.putInt("thReference", thReference);
                        editor.apply();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                waterValue1 = Integer.toString(hmReference);
                                if (!waterValue1.equals(""))
                                    tvValue1.setText(waterValue1);
                                waterValue2 = Integer.toString(thReference);
                                if (!waterValue2.equals(""))
                                    tvValue2.setText(waterValue2);
                            }
                        });
                    }
                }
            }
        }).start();
    }
    public void manuLightArray(){
        new Thread(new Runnable(){
            @Override
            public void run() { 
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;
                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL(popup.url +"getTableData?name=manage_light");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); 
                    urlConnection.setReadTimeout(15000); 
                    urlConnection.setRequestProperty("Connection", "close");
                    urlConnection.connect();
                    responseCode = urlConnection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK)
                        return;
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null)
                        return;
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder output = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                    reader.close();
                    inputStream.close();
                    urlConnection.disconnect();
                    if (output.length() == 0)
                        return;
                    String[] parts = output.toString().split("\\|");
                    JSONArray jsonArray = new JSONArray(parts[2]);
                    int[] uds = new int[jsonArray.length()];
                    String[] sts = new String[jsonArray.length()];
                    int[] lss = new int[jsonArray.length()];

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        uds[i] = jsonObject.getInt("ud");
                        sts[i] = jsonObject.getString("st");
                        lss[i] = jsonObject.getInt("ls");
                    }
                    lightUdsRef.set(uds);
                    lightStsRef.set(sts);
                    lightLssRef.set(lss);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    responseCode = 0;
                    if (getContext() != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "다시 시도", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException jsonException){
                    jsonException.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(responseCode == HttpURLConnection.HTTP_OK){
                    int[] uds = lightUdsRef.get();
                    String[] sts = lightStsRef.get();
                    int[] lss = lightLssRef.get();
                    JSONArray udJsonArray = new JSONArray();
                    if (uds != null) {
                        for (int ud : uds) {
                            udJsonArray.put(ud);
                        }
                    }
                    JSONArray stJsonArray = new JSONArray();
                    if (sts != null) {
                        for (String st : sts) {
                            stJsonArray.put(st);
                        }
                    }
                    JSONArray lsJsonArray = new JSONArray();
                    if (lss != null) {
                        for (int ls : lss) {
                            lsJsonArray.put(ls);
                        }
                    }
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("otReference", otReference);
                    editor.apply();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lightDataList.clear();
                            edLight1 = Integer.toString(otReference);
                            if (!edLight1.equals(""))
                                edValue1.setText(edLight1);
                            for (int i = 0; i < udJsonArray.length(); i++) {
                                try {
                                    DataValue dataValue = new DataValue();
                                    dataValue.setDate(udJsonArray.get(i).toString());
                                    dataValue.setTime(stJsonArray.get(i).toString());
                                    dataValue.setValue(lsJsonArray.get(i).toString());
                                    lightDataList.add(dataValue);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            dataAdapter.setDataList(lightDataList);
                            dataAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }
    public void autoLightData(){
        new Thread(new Runnable() {
            @Override
            public void run() { 
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;
                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL(popup.url +"getTableData?name=manage_auto");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); 
                    urlConnection.setReadTimeout(15000); 
                    urlConnection.setRequestProperty("Connection", "close");
                    urlConnection.connect();
                    
                    responseCode = urlConnection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK)
                        return;
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null)
                        return;
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder output = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                    reader.close();
                    inputStream.close();
                    urlConnection.disconnect();
                    if (output.length() == 0)
                        return;
                    String[] parts = output.toString().split("\\|");
                    JSONArray jsonArray = new JSONArray(parts[2]);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    ltReference = jsonObject.getInt("lt");
                    drReference = jsonObject.getInt("dr");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    responseCode = 0;
                    if (getContext() != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "다시 시도", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }catch (JSONException jsonException){
                    jsonException.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("ltReference", ltReference);
                        editor.putInt("drReference", drReference);
                        editor.apply();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lightValue1 = Integer.toString(ltReference);
                                if (!lightValue1.equals(""))
                                    tvValue1.setText(lightValue1);
                                lightValue2 = Integer.toString(drReference);
                                if (!lightValue2.equals(""))
                                    tvValue2.setText(lightValue2);
                            }
                        });
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

    private boolean isNotNullOfDelete() {return !deleteIndexList.isEmpty();}
    private void clearValue() {
        value1.setText("");
        value2.setText("");
        value3.setText("");
    }
    private void deleteWaterRecord(String data) {
        if (!data.equals(""))
            deleteList.add(waterDataList.get(Integer.parseInt(data)));
    }
    private void deleteLightRecord(String data) {
        if (!data.equals(""))
            deleteList.add(lightDataList.get(Integer.parseInt(data)));
    }
    @Override
    public void onResume() {
        super.onResume();
        tvHumid.setText(humid);
        tvLight.setText(light);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        waterState = sharedPreferences.getBoolean("WaterCheckBoxState", false);
        lightState = sharedPreferences.getBoolean("LightCheckBoxState", false);
        checkBoxWater.setChecked(waterState);
        checkBoxLight.setChecked(lightState);
    }
    private void initView(boolean isChecked) {
        if (Objects.equals(curType, WATER)) {
            if (isChecked) {
                includeView.setVisibility(View.GONE);
                tableCenter.setVisibility(View.VISIBLE);
                AutoWaterButton.setVisibility(View.VISIBLE);
                AutoLightButton.setVisibility(View.GONE);
                edValue2.setEnabled(true);
            } else {
                includeView.setVisibility(View.VISIBLE);
                tableCenter.setVisibility(View.GONE);
                edValue2.setEnabled(false);
            }
        }
    }
    private boolean isNotNullOfAutoValue(String value1, String value2) {
        return !value1.equals("") && !value2.equals("");
    }
    private boolean isNotNullOfManuValue(String value1) {
        return !value1.equals("");
    }
    private void checkConnectAndRun( Thread targetThread ){
        if(popup.CONNECT_STATE)
            targetThread.start();
        else
            Toast.makeText(getContext(), "연결되어있지 않음", Toast.LENGTH_SHORT).show();
    }
}