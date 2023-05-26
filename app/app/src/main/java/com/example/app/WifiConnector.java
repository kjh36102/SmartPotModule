package com.example.app;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WifiConnector {
    private Context context;
    private WifiManager wifiManager;

    public interface ConnectionCallback {
        void onConnectionResult(boolean isConnected);
    }

    public WifiConnector(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void connectToNetwork(String ssid, String password, ConnectionCallback callback) {
        System.out.println("connectTo");

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();

        List<WifiNetworkSuggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);

        int status = wifiManager.addNetworkSuggestions(suggestions);

        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            callback.onConnectionResult(false);
            return;
        }

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            System.out.println("쓰레드 진입");
            while (System.currentTimeMillis() - startTime < 60000) {  // 60 seconds timeout
                System.out.println("쓰레드 while");
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                System.out.println(connectionInfo.getSSID());
                if (connectionInfo != null && connectionInfo.getSSID().equals("\"" + ssid + "\"")) {
                    System.out.println("쓰레드 if");
                    callback.onConnectionResult(true);
                    return;
                }
                try {
                    Thread.sleep(1000);  // Check every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Timed out, unsuccessful
            callback.onConnectionResult(false);

        }).start();
    }
}
