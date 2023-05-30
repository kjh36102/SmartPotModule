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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class WifiConnectionManager {

    /**
     * 이 클래스는 네트워크 연결에 필요한 권한과 관련된 메소드만 모아놓은 것이다.
     */
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

        /**
         * 이 메소드는 실행에 필요한 권한이 모두 있는지 확인한다.
         *
         * @return 모두 있으면 true, 하나라도 없으면 false
         */
        public boolean hasAll() {
            for (String permission : PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 이 메소드는 실행에 필요한 권한을 모두 요청한다.
         */
        public void requestAll() {
            ActivityCompat.requestPermissions(this.activity, PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }


    private TextView statusTextview;
    private Activity activity;
    public Permission permission;
    public String arduinoIP;


    private Runnable onHotspotAvailable;
    private Runnable onHotspotUnAvailable;
    private Runnable onHotspotLost;
    private Runnable onExternalAvailable;
    private Runnable onExternalUnAvailable;
    private Runnable onExternalLost;
    private Runnable onUdpSuccess;
    private Runnable onUdpTimeout;
    private Runnable onUdpError;
    private Runnable onPingSuccess;
    private Runnable onPingFailed;
    private Runnable onPingTimeout;
    private Runnable onPingError;
    private Exception currentException;


    private ConnectivityManager hotspotConnManager;
    private ConnectivityManager.NetworkCallback hotspotNetworkCallback;

    private ConnectivityManager externalConnManager;
    private ConnectivityManager.NetworkCallback externalNetworkCallback;


    /**
     * 이 생성자는 WifiConnectionManager의 인스턴스를 생성한다.
     *
     * @param activity       넘겨받을 Activity 인스턴스
     * @param statusTextview 상태를 표시할 TextView의 인스턴스
     */
    WifiConnectionManager(Activity activity, TextView statusTextview) {
        this.permission = new Permission(activity);
        this.activity = activity;
        this.statusTextview = statusTextview;
//            this.externalSocket = new DatagramSocket(12345);

    }

    /**
     * 이 메소드는 생성자에서 넘겨준 상태표시 TextView의 값을 업데이트 한다.
     *
     * @param statusText 새로운 상태 메시지
     */
    public void setStatusText(String statusText) {  //runOnUiThread를 통해 업데이트
        this.activity.runOnUiThread(() -> this.statusTextview.setText(statusText));
    }

    /**
     * 이 메소드는 hotspot과 external의 ConnectivityManager와 NetworkCallback을 초기화한다. 직접 호출하지 않는다.
     */
    private void initConn(){
        if (hotspotConnManager != null && hotspotNetworkCallback != null) {
            hotspotConnManager.unregisterNetworkCallback(hotspotNetworkCallback);
        }

        if (externalConnManager!= null && externalNetworkCallback != null) {
            externalConnManager.unregisterNetworkCallback(externalNetworkCallback);
        }
    }

    /**
     * 이 메소드는 와이파이를 변경하고 상태에따라 다른 콜백을 호출한다. 직접 호출하지 않는다.
     *
     * @param ssid          와이파이의 ssid
     * @param pw            와이파이의 pw
     * @param timeout       타임아웃
     * @param onSuccess     성공시 콜백
     * @param onUnAvailable 실패시 콜백
     * @param onLost        연결끊길시 콜백
     */
    private void connectToNetwork(String ssid, String pw, int timeout, Runnable onSuccess, Runnable onUnAvailable, Runnable onLost) {

        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid);

        if (pw != null) builder.setWpa2Passphrase(pw);

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

    /**
     * 이 메소드는 핫스팟에 연결하기위해 호출한다.
     *
     * @param ssid    핫스팟의 ssid
     * @param timeout 타임아웃
     */
    public void connectToHotspot(String ssid, int timeout) {
        initConn();
        connectToNetwork(ssid, null, timeout, this.onHotspotAvailable, this.onHotspotUnAvailable, this.onHotspotLost);
    }

    /**
     * 이 메소드는 외부와이파이에 연결하기위해 호출한다.
     *
     * @param ssid    외부와이파이의 ssid
     * @param pw      외부와이파이의 pw
     * @param timeout 타임아웃
     */
    public void connectToExternal(String ssid, String pw, int timeout) {
        connectToNetwork(ssid, pw, timeout, this.onExternalAvailable, this.onExternalUnAvailable, this.onExternalLost);
    }

    /**
     * 이 메소드는 아두이노로 외부 네트워크 정보를 전송한다.
     *
     * @param ssid        외부네트워크의 SSID
     * @param pw          외부네트워크의 비밀번호
     * @param connTimeout 연결 타임아웃(ms)
     * @param onSuccess   성공시 실행될 Runnable
     * @param onFailed    실패시 실행될 Runnable
     * @param onError     오류시 실행될 Runnable
     */
    public void sendExternalWifiInfo(String ssid, String pw, int connTimeout, Runnable onSuccess, Runnable onFailed, Runnable onError) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.4.1:12344/regExtWifi?ssid=" + ssid + "&pw=" + pw);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(connTimeout);
                connection.setReadTimeout(connTimeout);
                connection.connect();

                final int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) runIfNotNull(onSuccess);
                else runIfNotNull(onFailed);

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


    /**
     * 이 메소드는 아두이노의 Broadcast 메시지를 파싱하고 IP를 저장, 그 후 해당 IP를 타겟으로 삼아 ACK를 전송한다.
     *
     * @param timeout UDP 프로세스 타임아웃(ms)
     * @return 성공시 true, 실패시 false
     */
    public boolean listenAndACKtoUDP(int timeout) {

        try {   //UDP 리스닝 및 ACK응답

            DatagramSocket socket = new DatagramSocket(12345);
            socket.setSoTimeout(timeout);

            byte[] buffer = new byte[128];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            String arduinoIP;
            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                if (!message.contains("SmartPotModule")) continue;

                this.arduinoIP = arduinoIP = message.split(":")[1];
                System.out.println("아두이노IP: " + arduinoIP);
                break;
            }

            //arduinoIP에게 ACK 보내기
            byte[] sendData = "SmartPotModule:ACK".getBytes();

            InetAddress destAddr = InetAddress.getByName(arduinoIP);
            packet = new DatagramPacket(sendData, sendData.length, destAddr, 12345);

            for (int i = 0; i < 5; i++) socket.send(packet);

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
        }

        return false;
    }


    /**
     * 이 메소드는 최종연결을 확인하기 위해 아두이노의 웹서버에 접속한다.
     *
     * @param timeout      최종연결 타임아웃(ms)
     * @param rememberedIP 클래스 외부에 저장해놓은 아두이노의 외부네트워크 IP(즉 ping을보낼 타겟)
     * @return 성공시 true, 실패시 false
     */
    public boolean sendPing(int timeout, String rememberedIP) {

//        if (rememberedIP == null && this.arduinoIP == null) return false;

        try {
            if (rememberedIP == null) return false;

            URL url = new URL("http://" + rememberedIP + ":12345/hello");
//            URL url;
//            if (rememberedIP == null) url = new URL("http://" + this.arduinoIP + ":12345/hello");
//            else url = new URL("http://" + rememberedIP + ":12345/hello");


            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeout);
            connection.connect();

            final int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) runIfNotNull(this.onPingSuccess);
            else runIfNotNull(this.onPingFailed);

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

    /**
     * runnable이 null이 아닐경우만 실행한다. 직접 호출하지 않는다.
     *
     * @param runnable 실행할 runnable
     */
    private void runIfNotNull(Runnable runnable) {
        if (runnable != null) { runnable.run();}
    }

    /**
     * 이 메소드는 onError계열의 runnable 안에서 호출하여 현재 Exception객체를 획득한다. (MainActivity 예제 참고)
     *
     * @return 현재 발생한 (또는 마지막으로 발생한) Exception 객체
     */
    public synchronized Exception getCurrentException() {
        return this.currentException;
    }

    //이하부터 콜백 Runnable Setter 메소드
    public void setOnHotspotAvailable(Runnable runnable) {
        this.onHotspotAvailable = runnable;
    }

    public void setOnHotspotUnAvailable(Runnable runnable) {
        this.onHotspotUnAvailable = runnable;
    }

    public void setOnHotspotLost(Runnable runnable) {
        this.onHotspotLost = runnable;
    }

    public void setOnExternalAvailable(Runnable runnable) {
        this.onExternalAvailable = runnable;
    }

    public void setOnExternalUnAvailable(Runnable runnable) {
        this.onExternalUnAvailable = runnable;
    }

    public void setOnExternalLost(Runnable runnable) {
        this.onExternalLost = runnable;
    }

    public void setOnUdpSuccess(Runnable runnable) {
        this.onUdpSuccess = runnable;
    }

    public void setOnUdpTimeout(Runnable runnable) {
        this.onUdpTimeout = runnable;
    }

    public void setOnUdpError(Runnable runnable) {
        this.onUdpError = runnable;
    }

    public void setOnPingSuccess(Runnable runnable) {
        this.onPingSuccess = runnable;
    }

    public void setOnPingFailed(Runnable runnable) {
        this.onPingFailed = runnable;
    }

    public void setOnPingTimeout(Runnable runnable) {
        this.onPingTimeout = runnable;
    }

    public void setOnPingError(Runnable runnable) {
        this.onPingError = runnable;
    }

}

