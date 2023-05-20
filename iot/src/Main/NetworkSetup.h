#ifndef __NETWORK_SETUP_H__
#define __NETWORK_SETUP_H__

//----------------------------------------
#include <WiFi.h>
#include <WebServer.h>
#include "UDPLibrary.h"

#define SERVER_PORT 12345

//--------------------------------
#define LOGKEY "TaskConnWithApp.h"
#include "Logger.h"
//--------------------------------

const char* HOTSPOT_SSID = "SmartPotModule";
// const char* HOSTSPOT_PASSWORD = "";
String externalSSID = "";
String externalPassword = "";
IPAddress externalIP = IPAddress(0, 0, 0, 0);
IPAddress smartphoneIP = IPAddress(0, 0, 0, 0);

bool stateConnSameWifi = false;
bool stateReceivedWifiInfo = false;
bool stateConnExtWifi = false;

WebServer httpServer(SERVER_PORT);


#endif  // __NETWORK_SETUP_H__