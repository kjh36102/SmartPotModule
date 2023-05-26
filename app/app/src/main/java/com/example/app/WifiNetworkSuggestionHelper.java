package com.example.app;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;

import java.util.ArrayList;
import java.util.List;

public class WifiNetworkSuggestionHelper {
    private WifiManager wifiManager;
    private Context context;

    public WifiNetworkSuggestionHelper(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean addWifiNetworkSuggestion(String ssid, String passphrase) {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(passphrase)
                .build();

        List<WifiNetworkSuggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);

        int status = wifiManager.addNetworkSuggestions(suggestions);

        return status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS;
    }
}
