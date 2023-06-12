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
import java.util.function.ObjIntConsumer;

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
    private TableLayout tableCenter;
    private View includeView;
    private TextView tvCheck, tvValue, tvRange, tvValue1, tvValue2, tvValue3, tvValue4, tvValue5, edValue1,edValue2, tvLight, tvHumid;
    private static CheckBox checkBoxWater, checkBoxLight, CheckSetNearestWater, CheckSetNearestLight;
    private String curType = WATER;

    private Button btDelete, btRegister, receiveBt1, receiveBt2, receiveBt3, receiveBt4;;
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
                        CheckSetNearestWater.setVisibility(View.VISIBLE);
                        CheckSetNearestLight.setVisibility(View.GONE);
                        receiveBt1.setVisibility(View.VISIBLE);
                        receiveBt2.setVisibility(View.GONE);
                        receiveBt3.setVisibility(View.GONE);
                        receiveBt4.setVisibility(View.GONE);

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
                        CheckSetNearestWater.setVisibility(View.GONE);
                        CheckSetNearestLight.setVisibility(View.VISIBLE);
                        receiveBt1.setVisibility(View.GONE);
                        receiveBt2.setVisibility(View.GONE);
                        receiveBt3.setVisibility(View.VISIBLE);
                        receiveBt4.setVisibility(View.GONE);
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
                // Tab이 선택 해제될 때 호출되는 메서드

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 이미 선택된 Tab이 다시 선택될 때 호출되는 메서드

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
            if (Objects.equals(curType, WATER)) {
                //현재 타입이 WATER인 경우
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
    public void manuWaterArray() {
        new Thread(new Runnable(){
            @Override
            public void run() { //http통신으로 받아옴
                System.out.println("manuWaterArray 시작 -----------------------");
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {
                    urlConnection = null;
                    reader = null;

                    URL url = new URL("http://cofon.xyz:9090/getTableData?name=manage_water"); //밑에 테이블
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); // 15 seconds
                    urlConnection.setReadTimeout(15000); // 15 seconds
                    urlConnection.setRequestProperty("Connection", "close");

                    urlConnection.connect();
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
                    if(parts[0].equals("err")) {
                        System.out.println("error받음");
                    }
                    JSONArray jsonArray = new JSONArray(parts[2]);

                    int[] uds = new int[jsonArray.length()];
                    String[] sts = new String[jsonArray.length()];
                    int[] wts = new int[jsonArray.length()];


                    //sample data test
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        System.out.println("두 번째 jsonObject: " + jsonObject);

                        uds[i] = jsonObject.getInt("ud");
                        sts[i] = jsonObject.getString("st");
                        wts[i] = jsonObject.getInt("wt");

                        System.out.println("uds[i]: " + uds[i]);
                        System.out.println("sts[i]: " + sts[i]);
                        System.out.println("wts[i]: " + wts[i]);
                    }
                    waterUdsRef.set(uds);
                    waterStsRef.set(sts);
                    waterWtsRef.set(wts);


                } catch (IOException ioException) {
                    System.out.println("IO error: " + ioException.getMessage());
                    ioException.printStackTrace();

                } catch (JSONException jsonException){
                    System.out.println("JSON parsing error");
                    jsonException.printStackTrace();
                } catch (Exception e){
                    System.out.println("Exception error");
                    e.printStackTrace();
                } finally {
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
                }
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
                System.out.println("udJsonArray: " + udJsonArray);
                System.out.println("stJsonArray: " + stJsonArray);
                System.out.println("wtJsonArray: " + wtJsonArray);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("otReference", otReference);
                //배열을 저장해야함
                editor.putString("wateruds", udJsonArray.toString());
                editor.putString("watersts", stJsonArray.toString());
                editor.putString("waterwts", wtJsonArray.toString());
                editor.apply();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edWater1 = Integer.toString(otReference);
                        if (!edWater1.equals("")) {
                            edValue1.setText(edWater1);
                        }
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

                        //이후 ui적용

                    }
                });
            }
        }).start();
    }
    public void autoWaterData(){
        new Thread(new Runnable() {
            @Override
            public void run() { //http통신으로 받아옴
                System.out.println("autoWaterData시작 -----------------------");
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;

                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL("http://cofon.xyz:9090/getTableData?name=manage_auto");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); // 15 seconds
                    urlConnection.setReadTimeout(15000); // 15 seconds
                    urlConnection.setRequestProperty("Connection", "close");

                    urlConnection.connect();
                    // HTTP 상태 코드 확인
                    responseCode = urlConnection.getResponseCode();
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
                    if(parts[0].equals("err")) {
                        System.out.println("error받음");
                    }

                    JSONArray jsonArray = new JSONArray(parts[2]);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    System.out.println("첫번째 jsonObject: " + jsonObject);
                    //sample data test
                    hmReference = jsonObject.getInt("hm");
                    thReference = jsonObject.getInt("th");
                    System.out.println("hm: " + hmReference);
                    System.out.println("th: " + thReference);

                } catch (IOException ioException) {
                    System.out.println("IO error: " + ioException.getMessage());
                    ioException.printStackTrace();
                }catch (JSONException jsonException){
                    System.out.println("JSON parsing error");
                    jsonException.printStackTrace();
                }catch (Exception e){
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
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("hmReference", hmReference);
                        editor.putInt("thReference", thReference);
                        editor.apply();
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        //이후 ui적용

                    }
                });
            }
        }).start();
    }
    public void manuLightArray(){
        new Thread(new Runnable(){
            @Override
            public void run() { //http통신으로 받아옴
                System.out.println("manuLightArray시작 -----------------------");
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;
                try {
                    urlConnection = null;
                    reader = null;

                    URL url = new URL("http://cofon.xyz:9090/getTableData?name=manage_light"); //밑에 테이블
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); // 15 seconds
                    urlConnection.setReadTimeout(15000); // 15 seconds
                    urlConnection.setRequestProperty("Connection", "close");

                    urlConnection.connect();
                    responseCode = urlConnection.getResponseCode();
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
                    if(parts[0].equals("err")) {
                        System.out.println("error받음");
                    }
                    JSONArray jsonArray = new JSONArray(parts[2]);

                    int[] uds = new int[jsonArray.length()];
                    String[] sts = new String[jsonArray.length()];
                    int[] lss = new int[jsonArray.length()];


                    //sample data test
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        System.out.println("두 번째 jsonObject: " + jsonObject);

                        uds[i] = jsonObject.getInt("ud");
                        sts[i] = jsonObject.getString("st");
                        lss[i] = jsonObject.getInt("ls");

                        System.out.println("uds[i]: " + uds[i]);
                        System.out.println("sts[i]: " + sts[i]);
                        System.out.println("wts[i]: " + lss[i]);
                    }
                    waterUdsRef.set(uds);
                    waterStsRef.set(sts);
                    waterWtsRef.set(lss);


                } catch (IOException ioException) {
                    System.out.println("IO error: " + ioException.getMessage());
                    ioException.printStackTrace();
                } catch (JSONException jsonException){
                    System.out.println("JSON parsing error");
                    jsonException.printStackTrace();
                } catch (Exception e){
                    System.out.println("Exception error");
                    e.printStackTrace();
                } finally {
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
                }
                int[] uds = waterUdsRef.get();
                String[] sts = waterStsRef.get();
                int[] lss = waterWtsRef.get();

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
                System.out.println("udJsonArray: " + udJsonArray);
                System.out.println("stJsonArray: " + stJsonArray);
                System.out.println("lsJsonArray: " + lsJsonArray);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("otReference", otReference);
                //배열을 저장해야함
                editor.putString("lightuds", udJsonArray.toString());
                editor.putString("lightsts", stJsonArray.toString());
                editor.putString("lightlss", lsJsonArray.toString());
                editor.apply();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        //이후 ui적용

                    }
                });
            }
        }).start();
    }
    public void autoLightData(){
        new Thread(new Runnable() {
            @Override
            public void run() { //http통신으로 받아옴
                System.out.println("autoLightData시작 -----------------------");
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int responseCode = 0;
                try {
                    urlConnection = null;
                    reader = null;
                    URL url = new URL("http://cofon.xyz:9090/getTableData?name=manage_auto");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000); // 15 seconds
                    urlConnection.setReadTimeout(15000); // 15 seconds
                    urlConnection.setRequestProperty("Connection", "close");

                    urlConnection.connect();
                    // HTTP 상태 코드 확인
                    responseCode = urlConnection.getResponseCode();
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
                    if(parts[0].equals("err")) {
                        System.out.println("error받음");
                    }

                    JSONArray jsonArray = new JSONArray(parts[2]);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    System.out.println("첫번째 jsonObject: " + jsonObject);
                    //sample data test
                    hmReference = jsonObject.getInt("lt");
                    thReference = jsonObject.getInt("dr");
                    System.out.println("lt: " + ltReference);
                    System.out.println("dr: " + drReference);

                } catch (IOException ioException) {
                    System.out.println("IO error: " + ioException.getMessage());
                    ioException.printStackTrace();
                }catch (JSONException jsonException){
                    System.out.println("JSON parsing error");
                    jsonException.printStackTrace();
                }catch (Exception e){
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
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("ltReference", ltReference);
                        editor.putInt("drReference", drReference);
                        editor.apply();
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        //이후 ui적용

                    }
                });
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
}