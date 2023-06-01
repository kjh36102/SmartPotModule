package com.example.app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatGPT {
    URL url;
    HttpURLConnection con;
    JSONObject input;
    String key;
    public ChatGPT(){
        try {
<<<<<<< Updated upstream
            url = new URL("https://api.openai.com/v1/completions");
            input = new JSONObject();
            input.put("model", "text-davinci-003");
            input.put("prompt", "");
            input.put("max_tokens", 2048);
            this.key = "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";
=======
            this.url = new URL("https://api.openai.com/v1/chat/completions");
            this.key = "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";
>>>>>>> Stashed changes
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String feedback(String name, double temperature, double humidity, double nitrogen, double phosphorus, double potassium, double ph, double ec, double lux) {
        this.input.put("prompt", "식물 "+name+"의 토양 온도는 "+temperature+"도, 토양 습도는 "+humidity+"%, 질소가 "+nitrogen+"mg/kg, 인이 "+phosphorus+"mg/kg, 칼륨이 "+potassium+" mg/kg, 토양 ph가 "+ph+" 전기 전도도는 "+ec+"us/cm, 광량은 "+lux+"lux이다. 문제가 있는 것을 분석해주고, 개선방안을 제안해줘.");
        return process();
    }
    public String recommand(String name){
        this.input.put("prompt", "식물 "+name+"의 추천 토양 온도(섭씨), 추천 토양 습도(%), 추천 N(mg/kg), 추천 P(mg/kg), 추천 K(mg/kg), 추천 토양산화도(ph), 추천 토양전기전도도(us/cm), 추천 광량(lux)의 각 수치를 범위로 만들어서 아래처럼 json을 만들어줘\n" +
                "{\"추천_토양온도\": {\"최소값\": xx,\"최대값\": xx,\"단위\": \"℃\"}, \"추천_토양습도\": {...}, \"추천_N\": {...}, \"추천_P\": {...}, \"추천_K\": {...}, \"추천_토양산화도\": {...}, \"추천_토양전기전도도\": {...},  \"추천_광량\": {...}}");
<<<<<<< Updated upstream
=======

        return process();
    }
    public String tips(String name){
        //this.input.put("prompt", "식물 "+name+"을 잘 키울 수 있는 팁을 알려줘");
        String prompt = "식물 "+name+"을 잘 키울 수 있는 팁을 알려줘";
        promptSet(prompt);
//        this.input.put("prompt", "식물 "+name+"을 온도, 습도, npk, 토양 전도도, 광량을 이용하여 잘 키울 수 있는 팁을 알려줘");
>>>>>>> Stashed changes
        return process();
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
                return "error";
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
                System.out.println(response.toString());
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
            return "error";
        }
    }
}