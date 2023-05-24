package com.example.app;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
public class ChatGPT {
    JSONObject userMessage;
    JSONArray messagesArray;
    JSONObject input;
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
//        this.input.put("prompt", "식물 "+name+"의 토양 온도는 "+temperature+"℃, 토양 습도는 "+humidity+"%, 질소가 "+nitrogen+"mg/kg, 인이 "+phosphorus+"mg/kg, 칼륨이 "+potassium+" mg/kg, 토양 ph가 "+ph+" 전기 전도도는 "+ec+"μS/cm, 광량은 "+lux+"lux이다. 이 식물의 상태를 보고 아래 json 데이터 형태로 총 100점 만점에 점수를 매겨줘.\n" +
//                "{\"총점\":\"...\"}");
        String prompt = "식물 "+name+"의 토양 온도는 "+temperature+"℃, 토양 습도는 "+humidity+"%, 질소가 "+nitrogen+"mg/kg, 인이 "+phosphorus+"mg/kg, 칼륨이 "+potassium+" mg/kg, 토양 ph가 "+ph+" 전기 전도도는 "+ec+"μS/cm, 광량은 "+lux+"lux이다. 이 식물의 상태를 보고 아래 json 데이터 형태로 총 100점 만점에 점수를 매겨줘. 부연설명 빼고 json데이터만 주면 돼.\n" +
                "{\"총점\":...}";
        promptSet(prompt);
        try{
            String response = process();
            System.out.println(response);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(response);
            JSONObject scoreResponse = (JSONObject) obj;
            System.out.println(scoreResponse);
            return scoreResponse;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("다시 시도해주세요.");
        }
        System.out.println("error");
        return new JSONObject();
    }
    public String feedback(String name, double temperature, double humidity, double nitrogen, double phosphorus, double potassium, double ph, double ec, double lux) {
        String prompt = "식물 "+name+"의 토양 온도는 "+temperature+"℃, 토양 습도는 "+humidity+"%, 질소가 "+nitrogen+"mg/kg, 인이 "+phosphorus+"mg/kg, 칼륨이 "+potassium+" mg/kg, 토양 ph가 "+ph+" 전기 전도도는 "+ec+"μS/cm, 광량은 "+lux+"lux이다. 문제가 있는 것을 분석해주고, 개선방안을 제안해줘.";
        promptSet(prompt);
        return process();
    }
    public String recommand(String name){
        this.input.put("prompt", "식물 "+name+"의 추천 토양 온도(℃), 추천 토양 습도(%), 추천 N(mg/kg), 추천 P(mg/kg), 추천 K(mg/kg), 추천 토양산화도(ph), 추천 토양전기전도도(μS/cm), 추천 광량(lux)의 각 수치를 범위로 만들어서 아래처럼 json을 만들어줘\n" +
                "{\"추천_토양온도\": {\"최소값\": xx,\"최대값\": xx,\"단위\": \"℃\"}, \"추천_토양습도\": {...}, \"추천_N\": {...}, \"추천_P\": {...}, \"추천_K\": {...}, \"추천_토양산화도\": {...}, \"추천_토양전기전도도\": {...},  \"추천_광량\": {...}}");

        return process();
    }
    public String tips(String name){
        this.input.put("prompt", "식물 "+name+"을 잘 키울 수 있는 팁을 알려줘");
        String prompt = "식물 "+name+"을 잘 키울 수 있는 팁을 알려줘";
        promptSet(prompt);
//        this.input.put("prompt", "식물 "+name+"을 온도, 습도, npk, 토양 전도도, 광량을 이용하여 잘 키울 수 있는 팁을 알려줘");
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
                System.out.println("Response Code : " + responseCode);
                return "error 응답 코드 200x";
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
                System.out.println(jsonResponse);
                //jsonResponse에서 text 부분만 추출하기 위한 과정
                JSONArray choices = (JSONArray) jsonResponse.get("choices");
                // choices 부분 추출하여 [] 배열 벗기기
                JSONObject choice = (JSONObject) choices.get(0);
                // 첫 번째 선택지에서 "message" 부분 추출하기
                JSONObject message = (JSONObject) choice.get("message");

                // "message" 부분에서 "content" 추출하기
                String content = (String) message.get("content");
                con.disconnect();
                return content;
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("다시 시도");
            return "error";
        }
    }
}