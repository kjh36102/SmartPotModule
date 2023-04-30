package com.example.test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
public class ChatGPT {

    public String callAPI(String key, String prompt) {
        try {
            URL url = new URL("https://api.openai.com/v1/completions");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Authorization", "Bearer " + key );
            con.setDoOutput(true);
            String input = "{\"model\": \"text-davinci-003\", \"prompt\": \""+prompt+"\",\"max_tokens\":2048}";
            // 입력 데이터의 디버그 출력
            System.out.println(input.toString());

            // 연결의 출력 스트림에 입력 데이터 쓰기
            try(OutputStream os = con.getOutputStream()) {
                byte[] inputBytes = input.getBytes("utf-8");
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

            // API 서버에서 응답을 읽기
            try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    //공백제거
                    response.append(responseLine.trim());
                }

                return (response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}
