package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {
    public static String callAPI(String key, String prompt) {
        try {
            URL url = new URL("https://api.openai.com/v1/completions");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Authorization", "Bearer " + key );
            con.setDoOutput(true);

            JSONObject input = new JSONObject();
            input.put("model", "text-davinci-003");
            input.put("prompt", prompt);
            input.put("max_tokens", 2048);
            System.out.println(input.toString());

            // 연결의 출력 스트림에 입력 데이터 쓰기
            try(OutputStream os = con.getOutputStream()) {
                String jsonRequest = input.toString();
                byte[] inputBytes = jsonRequest.getBytes("utf-8");
//                byte[] inputBytes = input.getBytes("utf-8");
                os.write(inputBytes, 0, inputBytes.length);
            }

            // API 서버에서 응답 코드 가져오기

            int responseCode = con.getResponseCode();
            if(responseCode != 200){
                return "error";
            }
            else{
                System.out.println("Response Code : " + responseCode);
            }

            // API 서버에서 응답을 읽습니다.
            try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
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
                ////
                JSONArray choices = (JSONArray) jsonResponse.get("choices");
                // choices 부분 추출하여 [] 배열 벗기기
                JSONObject choice = (JSONObject) choices.get(0);
                // text 필드 추출
                String text = (String) choice.get("text");
                return text;
//                return jsonResponse.toString(); 응답받은 json데이터 자체
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter : ");
        String prompt = scanner.nextLine();
        String result_api = callAPI("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", prompt);
        System.out.println(result_api);
    }
}
