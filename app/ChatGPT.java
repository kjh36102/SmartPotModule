package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//import java.util.Stack;
//import json;

//import org.json.JSONArray;
//import org.json.JSONObject;
public class Main {
//    public Main(String key) throws IOException {
//        URL url = new URL("https://api.openai.com/v1/completions");
//        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Content-Type", "application/json; utf-8");
//        con.setRequestProperty("Authorization", "Bearer " + key);
//        con.setDoOutput(true);
//        String prompt = "";
////        JSONObject jsonObject = new JSONObject();
////        jsonObject.put("key1", "value1");
////        jsonObject.put("key2", "value2");
////        String input = "{\"model\": \"text-davinci-003\", \"prompt\": \""+prompt+"\",\"max_tokens\":2048}";
//        JSONObject input = new JSONObject();
//        input.put("model", "text-davinci-003");
//        input.put("prompt", "");
//        input.put("max_tokens", 2048);
//        // 입력 데이터의 디버그 출력
//        System.out.println(input.toString());

//    }
//    public String MainAPI(String prompt){
//
//    }
    public static String callAPI(String key, String prompt) {
        try {
            URL url = new URL("https://api.openai.com/v1/completions");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Authorization", "Bearer " + key );
            con.setDoOutput(true);
//            String input = "{\"model\": \"text-davinci-003\", \"prompt\": \""+prompt+"\",\"max_tokens\":2048}";
            // 요청 매개변수로 JSON 문자열 생성
//            String input = "{\"prompt\": \"" + prompt + "\", \"temperature\":0.5, \"max_tokens\":1024, \"top_p\":1,\"frequency_penalty\":0, \"presence_penalty\":0 }";
            // 입력 데이터의 디버그 출력
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
//                System.out.println(obj);
                return obj.toString();
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
