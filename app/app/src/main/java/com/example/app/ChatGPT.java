package com.example.app;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ChatGPT {
    static JSONObject userMessage;
    static JSONArray messagesArray;
    static JSONObject input;
    URL url;
    HttpURLConnection con;
    String key;
    public ChatGPT(){
        try {
            this.url = new URL("https://api.openai.com/v1/chat/completions");
            this.key = "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";
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void promptSet(String prompt){
        userMessage = new JSONObject();
        userMessage.put("content", prompt);
        userMessage.put("role", "user");

        messagesArray = new JSONArray();
        messagesArray.add(userMessage);

        input = new JSONObject();
        input.put("model", "gpt-3.5-turbo");
        input.put("messages", messagesArray);
    }
    public JSONObject score(String name, double temperature, double humidity, double nitrogen, double phosphorus, double potassium, double ph, double ec, double lux){
        String prompt = "식물 "+name+"의 토양 온도(℃):"+temperature+", 수분부피/토양부피(%): "+humidity+", N(mg/kg):"+nitrogen+", P(mg/kg): "+phosphorus+", K(mg/kg):"+potassium+", 토양ph:"+ph+", 전기 전도도(μS/cm):"+ec+", 광량(lux):"+lux+"인데 이 식물의 상태를 보고 아래 json 데이터 형태로 총 100점 만점에 점수를 매겨줘. 부연설명 빼고 json데이터만 주면 돼.\n" +
                "{\"총점\":...}";
        String response = "";
        promptSet(prompt);
        try{
            response = process();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(response);
            JSONObject scoreResponse = (JSONObject) obj;
            return scoreResponse;
        }catch (ParseException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new JSONObject();
    }
    public String feedback(String name, double temperature, double humidity, double nitrogen, double phosphorus, double potassium, double ph, double ec, double lux) {
        String prompt = "식물 "+name+"의 토양 온도(℃):"+temperature+", 수분부피/토양부피(%): "+humidity+", N(mg/kg):"+nitrogen+", P(mg/kg): "+phosphorus+", K(mg/kg):"+potassium+", 토양ph:"+ph+", 전기 전도도(μS/cm):"+ec+", 광량(lux):"+lux+"인데 문제가 있는 것만 100자 이내로 요약 분석 및 개선방안을 제안, 100점만점으로 총점, 결과는 json으로 \n" +
                "{\"요약\":\"\",\"분석\":\"\",\"점수\":\"\",\"개선방안\":\"\"} 형태로 만들어줘.";
        promptSet(prompt);
        String text = process();
        return text;
    }
    public String recommand(String name){
        this.input.put("prompt", "식물 "+name+"의 추천 토양 온도(℃), 추천 토양 습도(%), 추천 N(mg/kg), 추천 P(mg/kg), 추천 K(mg/kg), 추천 토양산화도(ph), 추천 토양전기전도도(μS/cm), 추천 광량(lux)의 각 수치를 범위로 만들어서 아래처럼 json을 만들어줘\n" +
                "{\"추천_토양온도\": {\"최소값\": xx,\"최대값\": xx,\"단위\": \"℃\"}, \"추천_토양습도\": {...}, \"추천_N\": {...}, \"추천_P\": {...}, \"추천_K\": {...}, \"추천_토양산화도\": {...}, \"추천_토양전기전도도\": {...},  \"추천_광량\": {...}}");
        return process();
    }
    public String tips(String name){
        String prompt = "식물 "+name+"을 잘 키울 수 있는 팁을 알려줘";
        promptSet(prompt);
        String text = process();
        return text;
    }
    public String process(){
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Authorization", "Bearer " + key);
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {// 연결의 출력 스트림에 입력 데이터 쓰기
                String jsonRequest = input.toString();
                byte[] inputBytes = jsonRequest.getBytes("utf-8");
                os.write(inputBytes, 0, inputBytes.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode != 200) { // API 서버에서 응답 코드 가져오기
                return "error 응답 코드 200x";
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {// API 서버에서 응답을 읽습니다.
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response.toString());
                JSONObject jsonResponse = (JSONObject) obj;
                JSONArray choices = (JSONArray) jsonResponse.get("choices");
                JSONObject choice = (JSONObject) choices.get(0);
                JSONObject message = (JSONObject) choice.get("message");
                String content = (String) message.get("content");
                con.disconnect();
                return content;
            }
        } catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }
}