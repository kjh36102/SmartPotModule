package com.example.app;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
public class ChatGPT {
    URL url;
    HttpURLConnection con;
    JSONObject input;
    String key;
    public ChatGPT(){
        try {
            url = new URL("https://api.openai.com/v1/completions");
            input = new JSONObject();
            input.put("model", "text-davinci-003");
            input.put("prompt", "");
            input.put("max_tokens", 2048);
            this.key = "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";
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject feedback(String name, double temperature, double humidity, double nitrogen, double phosphorus, double potassium, double ph, double ec, double lux) {
        this.input.put("prompt", "식물 "+name+"의 토양 온도는 "+temperature+"℃, 토양 습도는 "+humidity+"%, 질소가 "+nitrogen+"mg/kg, 인이 "+phosphorus+"mg/kg, 칼륨이 "+potassium+" mg/kg, 토양 ph가 "+ph+" 전기 전도도는 "+ec+"μS/cm, 광량은 "+lux+"lux이다. 아래 json을 보고 \"feedback\" 부분에 text형태로 개선방안을 제안해주고  \"score\" 부분에 정수 형태로 100점 만점으로 총점을 매겨줘. 아래처럼 json을 만들어줘\n" +
                "{\"feedback\":\"토양 상태는...\",\"score\":50 } ");
        String result = process();
        try {
            JSONParser parser = new JSONParser(); //json 파싱
            Object obj = parser.parse(result);
            JSONObject feedbackResponse = (JSONObject) obj;
            System.out.println(feedbackResponse);
            return feedbackResponse;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public JSONObject recommand(String name) {
        this.input.put("prompt", "식물 " + name + "의 추천 토양 온도(℃), 추천 토양 습도(%), 추천 N, P, K는 정수값으로(mg/kg), 추천 토양산화도(ph), 추천 토양전기전도도(μS/cm), 추천 광량(lux)의 각 수치를 범위로 만들어서 아래처럼 json을 만들어줘\n" +
                "{\"추천_토양온도\": {\"최소값\": xx,\"최대값\": xx,\"단위\": \"℃\"}, \"추천_토양습도\": {...}, \"추천_N\": {\"최소값\": 50,\"최대값\": 150,\"단위\": \"mg/kg\"}, \"추천_P\": {...}, \"추천_K\": {...}, \"추천_토양산화도\": {...}, \"추천_토양전기전도도\": {...},  \"추천_광량\": {...}}");
//        return process();
        JSONObject result = validResponse(); //검증된 응답을 json으로 return
        System.out.println(result);
        return result;
    }
    public JSONObject validResponse(){
        int THREAD_COUNT = 3;
        String str1 = null;
        String str2 = null;
        String str3 = null;
        try {
            List<Callable<String>> tasks = new ArrayList<>();

            for (int i = 0; i < THREAD_COUNT; i++) {
                tasks.add(() -> process()); //각각 쓰레드로 process()를 병렬 처리
            }

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            try {
                List<Future<String>> futures = executorService.invokeAll(tasks); //작업 목록을 실행하고, Future 객체의 리스트로 결과를 받음

                str1 = futures.get(0).get();
                str2 = futures.get(1).get();
                str3 = futures.get(2).get();

                JSONParser parser = new JSONParser(); //json 파싱
                Object obj1 = parser.parse(str1);
                Object obj2 = parser.parse(str2);
                Object obj3 = parser.parse(str3);

                JSONObject jsonResponse1 = (JSONObject) obj1;
                JSONObject jsonResponse2 = (JSONObject) obj2;
                JSONObject jsonResponse3 = (JSONObject) obj3;

                //쓰레드로 구현할지 고민중
                if(!isValidResponse(jsonResponse1)){
                    jsonResponse1 = null;
                }
                if(!isValidResponse(jsonResponse2)){
                    jsonResponse1 = null;
                }
                if(!isValidResponse(jsonResponse3)){
                    jsonResponse1 = null;
                }

                // 최종적으로 하나의 유효한 응답만 반환
                if (jsonResponse1 != null) {
                    return jsonResponse1;
                }
                if (jsonResponse2 != null) {
                    return jsonResponse2;
                }
                if (jsonResponse3 != null) {
//                    return jsonResponse3.toJSONString();
                    return jsonResponse3;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private boolean isValidResponse(JSONObject jsonResponse) {
        // 원하지 않는 값을 포함하고 있는지 확인하는 로직을 구현
        // 예를 들어, 원하지 않는 값이 포함되어 있다면 false를 반환하고, 그렇지 않으면 true를 반환

        return true; // 임시로 true 반환하도록 설정
    }
    public String process(){
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Authorization", "Bearer " + key);
            con.setDoOutput(true);
            System.out.println(input.toString());
            // 연결의 출력 스트림에 입력 데이터 쓰기

            try (OutputStream os = con.getOutputStream()) {
                String jsonRequest = input.toString();
                byte[] inputBytes = jsonRequest.getBytes("utf-8");
                os.write(inputBytes, 0, inputBytes.length);
            }

            // API 서버에서 응답 코드 가져오기

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                return null;
            } else {
                System.out.println("Response Code : " + responseCode);
            }

            // API 서버에서 응답을 읽습니다.
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    //공백제거
                    response.append(responseLine.trim());
                }
                //String 데이터를 Object로 변환
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response.toString());
                // Object 데이터를 JSONObject로 변환
                JSONObject jsonResponse = (JSONObject) obj;
                //jsonResponse에서 text 부분만 추출하기 위한 과정
                JSONArray choices = (JSONArray) jsonResponse.get("choices");
                // choices 부분 추출하여 [] 배열 벗기기
                JSONObject choice = (JSONObject) choices.get(0);
                // text 필드 추출
                String text = (String) choice.get("text");
                con.disconnect();
                return text;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

