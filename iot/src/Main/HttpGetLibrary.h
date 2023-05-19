#ifndef __HTTP_GET_LIBRARY_H__
#define __HTTP_GET_LIBRARY_H__

//-------------------------------

#include <map>
#include <WiFi.h>
#include <WiFiClient.h>
#include <WiFiServer.h>

//--------------------------------
#define LOGKEY "HttpGetLibrary.h"
#include "Logger.h"
//--------------------------------

/**
WiFiClient 객체로부터 들어온 HTTP GET 요청의 쿼리 값을 파싱해 맵으로 리턴하는 함수

[예시 코드]
std::map<String, String> queryMap = getQueryMap(client);
for (const auto& kv : queryMap) {
  Serial.print(kv.first);
  Serial.print(": ");
  Serial.println(kv.second);
}
*/
std::map<String, String> getQueryMap(WiFiClient& client, size_t bufferSize) {
  String request = client.readStringUntil('\r');
  byte queryStartIndex = request.indexOf("GET /?") + String("GET /?").length();
  byte queryEndIndex = request.indexOf(" ", queryStartIndex);

  String query = request.substring(queryStartIndex, queryEndIndex);

  LOGF("query: %s\n", query.c_str());
  LOGF("query len: %d\n", query.length());

  std::map<String, String> queryMap;

  if (query.length() == 0 || strcmp(query.c_str(), "favicon.ico") == 0) return queryMap;

  // Adjust query length if it exceeds the buffer size
  if (query.length() > bufferSize) {
    query = query.substring(0, bufferSize);
  }

  while (query.length() > 0) {
    byte delimiterIndex = query.indexOf("&");
    String keyValue;
    if (delimiterIndex != -1) {
      keyValue = query.substring(0, delimiterIndex);
      query = query.substring(delimiterIndex + 1);
    } else {
      keyValue = query;
      query = "";
    }

    byte equalsIndex = keyValue.indexOf("=");
    if (equalsIndex != -1) {
      String key = keyValue.substring(0, equalsIndex);
      String value = keyValue.substring(equalsIndex + 1);
      queryMap[key] = value;
    }
  }

  return queryMap;
}


void sendHttpResponse(WiFiClient& client, unsigned int code, const char* msg) {
  client.println("HTTP/1.1 " + String(code) + " OK");
  client.println("Content-Type: text/plain");
  client.println("Connection: close");
  client.println();
  client.println(String(code) + ":" + msg);
}


//-------------------------------
#endif  // __HTTP_GET_LIBRARY_H__
