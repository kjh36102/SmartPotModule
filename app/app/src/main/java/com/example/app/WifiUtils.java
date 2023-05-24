package com.example.app;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class WifiUtils {

    private WifiManager wifiManager;

    public WifiUtils(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean connectToWifi(String ssid, String password) {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", popup.ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", popup.pw);

        int networkId = wifiManager.addNetwork(wifiConfig);
        boolean isConnected = wifiManager.enableNetwork(networkId, true);

        return isConnected;
    }
}
