package com.example.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class WifiConnectionManager {

    class Permission {
        private static final int PERMISSION_REQUEST_CODE = 10;
        Activity activity;

        Permission(Activity activity) {
            this.activity = activity;
        }

        private final String[] PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.WRITE_SETTINGS
        };

        public boolean hasAll() {
            for (String permission : PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
            return true;
        }

        public void requestAll() {
            ActivityCompat.requestPermissions(this.activity, PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    private TextView statusTextview;
    private Activity activity;
    public Permission permission;
    public String arduinoIP;

    private Runnable onHotspotAvailable,onHotspotUnAvailable, onHotspotLost;
    private Runnable onExternalAvailable,onExternalUnAvailable,onExternalLost;
    private Runnable onUdpSuccess,onUdpTimeout,onUdpError;
    private Runnable onPingSuccess,onPingFailed,onPingTimeout,onPingError;
    private Exception currentException;


    private ConnectivityManager hotspotConnManager;
    private ConnectivityManager.NetworkCallback hotspotNetworkCallback;
    private ConnectivityManager externalConnManager;
    private ConnectivityManager.NetworkCallback externalNetworkCallback;

    WifiConnectionManager(Activity activity, TextView statusTextview) {
        this.permission = new Permission(activity);
        this.activity = activity;
        this.statusTextview = statusTextview;
    }

    public void setStatusText(String statusText) {  
        this.activity.runOnUiThread(() -> this.statusTextview.setText(statusText));
    }

    private void initConn(){
        if (hotspotConnManager != null && hotspotNetworkCallback != null)
            hotspotConnManager.unregisterNetworkCallback(hotspotNetworkCallback);
        if (externalConnManager!= null && externalNetworkCallback != null)
            externalConnManager.unregisterNetworkCallback(externalNetworkCallback);
    }

    private void connectToNetwork(String ssid, String pw, int timeout, Runnable onSuccess, Runnable onUnAvailable, Runnable onLost) {
        initConn();

    
        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid);

        if (pw != null)
            builder.setWpa2Passphrase(pw);
        WifiNetworkSpecifier specifier = builder.build();
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build();

        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);  
                runIfNotNull(onSuccess);
            }
            @Override
            public void onUnavailable() {
                super.onUnavailable();
                runIfNotNull(onUnAvailable);
            }
            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                runIfNotNull(onLost);
            }
        };

        connectivityManager.requestNetwork(networkRequest, networkCallback, timeout);
        if(pw == null){
            this.hotspotConnManager = connectivityManager;
            this.hotspotNetworkCallback = networkCallback;
        }else{
            this.externalConnManager = connectivityManager;
            this.externalNetworkCallback = networkCallback;
        }
    }

    public void connectToHotspot(String ssid, int timeout) { 
        connectToNetwork(ssid, null, timeout, this.onHotspotAvailable, this.onHotspotUnAvailable, this.onHotspotLost);
    }

    public void connectToExternal(String ssid, String pw, int timeout) {
        connectToNetwork(ssid, pw, timeout, this.onExternalAvailable, this.onExternalUnAvailable, this.onExternalLost);
    }

    public void sendExternalWifiInfo(String ssid, String pw, int connTimeout, Runnable onSuccess, Runnable onFailed, Runnable onError) {
        new Thread(() -> {  
            try {
                URL url = new URL("http://192.168.4.1:12344/regExtWifi?ssid=" + ssid + "&pw=" + pw);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(connTimeout);
                connection.setReadTimeout(connTimeout);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseData.append(line);
                }
                reader.close();
                String parsed[] = responseData.toString().split("\\|");
                if( parsed[0].equals("ok")) {
                    runIfNotNull(onSuccess);
                    this.statusTextview.setText("외부와이파이 연결 성공");
                }
                else if(parsed[0].equals("err")){
                    runIfNotNull(onFailed);
                    if(parsed[1].equals("0"))
                        this.statusTextview.setText("쿼리 인자 부족");
                    else if(parsed[1].equals("1"))
                        this.statusTextview.setText("연결모드가 아님");
                    else if(parsed[1].equals("2"))
                        this.statusTextview.setText("외부네트워크 연결 실패");
                }
            } catch (Exception e) {
                e.printStackTrace();
                synchronized (this) {
                    this.currentException = e;
                }
                runIfNotNull(onError);
            }
        }
        ).start();
    }

    public boolean listenAndACKtoUDP(int timeout) { 
            DatagramSocket socket=null;
        try {   

            socket = new DatagramSocket(12345);
            socket.setSoTimeout(timeout);
            byte[] buffer = new byte[128];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            String arduinoIP;
            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (!message.contains("SmartPotModule"))
                    continue;
                this.arduinoIP = arduinoIP = message.split(":")[1];
                break;
            }
            byte[] sendData = "SmartPotModule:ACK".getBytes(); 

            InetAddress destAddr = InetAddress.getByName(arduinoIP);
            packet = new DatagramPacket(sendData, sendData.length, destAddr, 12345);
            for (int i = 0; i < 5; i++)
                socket.send(packet);
            socket.close();
            runIfNotNull(this.onUdpSuccess);
            return true;
        } catch (SocketTimeoutException e) {  
            runIfNotNull(this.onUdpTimeout);
        } catch (Exception e) {
            e.printStackTrace();
            synchronized (this) {
                this.currentException = e;
            }
            runIfNotNull(this.onUdpError);
        } finally{
            if(socket !=null)
                socket.close();
        }
        return false;
    }

    public boolean sendPing(int timeout, String rememberedIP) {
        try {
            if (rememberedIP == null) return false;
            URL url = new URL("http://" + rememberedIP + ":12345/hello");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeout);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder responseData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }
            reader.close();
            String parsed[] = responseData.toString().split("\\|");

            if( parsed[0].equals("ok")) {
                runIfNotNull(this.onPingSuccess);
                if(parsed[1].equals("0"))
                    if(this.statusTextview != null)
                        this.statusTextview.setText("최종 연결 성공");
                else if(parsed[1].equals("1"))
                    if(this.statusTextview != null)
                        this.statusTextview.setText("최종 연결 성공, 그러나 인터넷 안됨");
                else if(parsed[1].equals("2"))
                    if(this.statusTextview != null)
                        this.statusTextview.setText("핑");
            }
            else if(parsed[0].equals("err")){
                runIfNotNull(this.onPingFailed);
                if(this.statusTextview != null)
                    this.statusTextview.setText("아직 외부네트워크 연결 안됨");
            }
            return true;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            runIfNotNull(this.onPingTimeout);
        } catch (Exception e) {
            e.printStackTrace();
            synchronized (this) {
                this.currentException = e;
            }
            runIfNotNull(this.onPingError);
        }
        return false;
    }

    private void runIfNotNull(Runnable runnable) {
        if (runnable != null)
            runnable.run();
    }
    public synchronized Exception getCurrentException() {
        return this.currentException;               
    }
    
    public void setOnHotspotAvailable(Runnable runnable) {this.onHotspotAvailable = runnable;}
    public void setOnHotspotUnAvailable(Runnable runnable) { this.onHotspotUnAvailable = runnable;}
    public void setOnHotspotLost(Runnable runnable) {this.onHotspotLost = runnable;}
    public void setOnExternalAvailable(Runnable runnable) {this.onExternalAvailable = runnable;}
    public void setOnExternalUnAvailable(Runnable runnable) {this.onExternalUnAvailable = runnable;}
    public void setOnExternalLost(Runnable runnable) {this.onExternalLost = runnable;}
    public void setOnUdpSuccess(Runnable runnable) {this.onUdpSuccess = runnable;}
    public void setOnUdpTimeout(Runnable runnable) {this.onUdpTimeout = runnable;}
    public void setOnUdpError(Runnable runnable) {this.onUdpError = runnable;}
    public void setOnPingSuccess(Runnable runnable) {this.onPingSuccess = runnable;}
    public void setOnPingFailed(Runnable runnable) {this.onPingFailed = runnable;}
    public void setOnPingTimeout(Runnable runnable) {this.onPingTimeout = runnable;}
    public void setOnPingError(Runnable runnable) {this.onPingError = runnable;}
}

