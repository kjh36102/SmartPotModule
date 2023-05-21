package com.example.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiConnectionManager {
    private static final String NETWORK_SSID = "SmartPotModule";
    private static final String NETWORK_PASSWORD = "your_password";
    private static final long WIFI_CHECK_INTERVAL = 5000; // Check interval in milliseconds

    private Context context;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private boolean isCheckingWifi;
    private Thread wifiCheckThread;

    public WifiConnectionManager(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void startWifiCheck() {
        isCheckingWifi = true;
        wifiCheckThread = new Thread(new WifiCheckRunnable());
        wifiCheckThread.start();
    }

    public void stopWifiCheck() {
        isCheckingWifi = false;
        if (wifiCheckThread != null) {
            wifiCheckThread.interrupt();
        }
    }

    private class WifiCheckRunnable implements Runnable {
        @Override
        public void run() {
            while (isCheckingWifi) {
                try {
                    Thread.sleep(WIFI_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Check if connected to SmartPotModule network
                if (!isConnectedToNetwork(NETWORK_SSID)) {
                    popup.connText.setText("아두이노 핫스팟 끊김");
                    // SmartPotModule network is not available, attempt to connect to a new network
                    connectToNewWifi(popup.ssid, popup.pw);
                }
            }
        }
    }

    private void connectToNewWifi(String ssid, String password) {
        // Disconnect from the current network if connected
        wifiManager.disconnect();

        // Create a new Wi-Fi configuration
        WifiConfiguration newNetworkConfig = createWifiConfiguration(ssid, password);

        // Add the new network configuration
        int networkId = wifiManager.addNetwork(newNetworkConfig);

        // Enable and connect to the new network
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();

        // Wait for the connection to be established
        try {
            Thread.sleep(5000); // Adjust the waiting time as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the device is connected to the desired network
        if (isConnectedToNetwork(ssid)) {
            // Connection successful
            popup.connText.setText("와이파이 연결성공");
            popup.connCheck=true;
        } else {
            // Connection failed
            popup.connText.setText("와이파이 연결실패");
        }
    }

    private WifiConfiguration createWifiConfiguration(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.preSharedKey = "\"" + password + "\"";
        return config;
    }

    private boolean isConnectedToNetwork(String networkSSID) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID().equals("\"" + networkSSID + "\"")) {
                return true;
            }
        }
        return false;
    }
}
