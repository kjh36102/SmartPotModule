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

        public void requestAll() {//이 메소드는 실행에 필요한 권한을 모두 요청한다.
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

    WifiConnectionManager(Activity activity, TextView statusTextview) {//이 생성자는 WifiConnectionManager의 인스턴스를 생성한다.//이 생성자는 WifiConnectionManager의 인스턴스를 생성한다.
        this.permission = new Permission(activity);
        this.activity = activity;
        this.statusTextview = statusTextview;
    }

    public void setStatusText(String statusText) {  //runOnUiThread를 통해 업데이트, 이 메소드는 생성자에서 넘겨준 상태표시 TextView의 값을 업데이트 한다.
        this.activity.runOnUiThread(() -> this.statusTextview.setText(statusText));
    }

    private void initConn(){//이 메소드는 hotspot과 external의 ConnectivityManager와 NetworkCallback을 초기화한다. 직접 호출하지 않는다.
        if (hotspotConnManager != null && hotspotNetworkCallback != null)
            hotspotConnManager.unregisterNetworkCallback(hotspotNetworkCallback);
        if (externalConnManager!= null && externalNetworkCallback != null)
            externalConnManager.unregisterNetworkCallback(externalNetworkCallback);
    }

    private void connectToNetwork(String ssid, String pw, int timeout, Runnable onSuccess, Runnable onUnAvailable, Runnable onLost) {
        initConn();

    //이 메소드는 와이파이를 변경하고 상태에따라 다른 콜백을 호출한다. 직접 호출하지 않는다.
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
                connectivityManager.bindProcessToNetwork(network);  //통신 타겟 네트워크를 현재 연결하려는 네트워크로 설정
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

    public void connectToHotspot(String ssid, int timeout) { //이 메소드는 핫스팟에 연결하기위해 호출한다.
        connectToNetwork(ssid, null, timeout, this.onHotspotAvailable, this.onHotspotUnAvailable, this.onHotspotLost);
    }

    public void connectToExternal(String ssid, String pw, int timeout) {//이 메소드는 외부와이파이에 연결하기위해 호출한다.
        connectToNetwork(ssid, pw, timeout, this.onExternalAvailable, this.onExternalUnAvailable, this.onExternalLost);
    }

    public void sendExternalWifiInfo(String ssid, String pw, int connTimeout, Runnable onSuccess, Runnable onFailed, Runnable onError) {
        new Thread(() -> {  //이 메소드는 아두이노로 외부 네트워크 정보를 전송한다.
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

    public boolean listenAndACKtoUDP(int timeout) { //이 메소드는 아두이노의 Broadcast 메시지를 파싱하고 IP를 저장, 그 후 해당 IP를 타겟으로 삼아 ACK를 전송한다.
            DatagramSocket socket=null;
        try {   //UDP 리스닝 및 ACK응답

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
            byte[] sendData = "SmartPotModule:ACK".getBytes(); //arduinoIP에게 ACK 보내기

            InetAddress destAddr = InetAddress.getByName(arduinoIP);
            packet = new DatagramPacket(sendData, sendData.length, destAddr, 12345);
            for (int i = 0; i < 5; i++)
                socket.send(packet);
            socket.close();
            runIfNotNull(this.onUdpSuccess);
            return true;
        } catch (SocketTimeoutException e) {  //타임아웃을 먼저 예외처리해야함\
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

    public boolean sendPing(int timeout, String rememberedIP) {//이 메소드는 최종연결을 확인하기 위해 아두이노의 웹서버에 접속한다.
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

    private void runIfNotNull(Runnable runnable) {//runnable이 null이 아닐경우만 실행한다. 직접 호출하지 않는다.
        if (runnable != null)
            runnable.run();
    }
    public synchronized Exception getCurrentException() {//이 메소드는 onError계열의 runnable 안에서 호출하여 현재 Exception객체를 획득한다. (MainActivity 예제 참고)
        return this.currentException;               //return 현재 발생한 (또는 마지막으로 발생한) Exception 객체
    }
    //이하부터 콜백 Runnable Setter 메소드
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

