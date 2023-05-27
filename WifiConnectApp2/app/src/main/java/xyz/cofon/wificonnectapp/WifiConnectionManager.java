package xyz.cofon.wificonnectapp;

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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
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
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.WRITE_SETTINGS
        };

        /**
         * 이 메소드는 실행에 필요한 권한이 모두 있는지 확인한다.
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
    private Runnable onSendWebMessageSuccess;
    private Runnable onSendWebMessageFailed;


    /**
     * 이 생성자는 WifiConnectionManager의 인스턴스를 생성한다.
     * @param activity 넘겨받을 Activity 인스턴스
     * @param statusTextview 상태를 표시할 TextView의 인스턴스
     */
    WifiConnectionManager(Activity activity, TextView statusTextview) {
        this.permission = new Permission(activity);
        this.activity = activity;
        this.statusTextview = statusTextview;
    }

    /**
     * 이 메소드는 생성자에서 넘겨준 상태표시 TextView의 값을 업데이트 한다.
     * @param statusText 새로운 상태 메시지
     */
    public void setStatusText(String statusText) {  //runOnUiThread를 통해 업데이트
        this.activity.runOnUiThread(() -> this.statusTextview.setText(statusText));
    }

    /**
     * 이 메소드는 핫스팟에 연결하고 실행 결과에 따라 각기 다른 콜백함수를 호출한다.
     * @param hotspotSsid 연결할 핫스팟의 SSID
     */
    public void connectToHotspot(String hotspotSsid) {
        //WifiNetworkSpecifier 객체 생성을 통해 와이파이 연결 수립을 정의함
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid(hotspotSsid)
                .build();

        //NetworkRequest에 위의 specifier객체를 념겨줌으로써 연결수립을 요청함
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build();

        //Activity의 시스템 서비스로부터 CONNECTIVITY_SERVICE를 가져와 ConnectivityManager 객체를 참조함
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);

        //콜백함수 정의
        ConnectivityManager.NetworkCallback hotspotCallback = new ConnectivityManager.NetworkCallback() {
            private Runnable onHotspotAvailable = WifiConnectionManager.this.onHotspotAvailable;
            private Runnable onHotspotUnAvailable = WifiConnectionManager.this.onHotspotUnAvailable;
            private Runnable onHotspotLost = WifiConnectionManager.this.onHotspotLost;

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
                if (this.onHotspotAvailable != null)
                    this.onHotspotAvailable.run();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                if (this.onHotspotUnAvailable != null)
                    this.onHotspotUnAvailable.run();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (this.onHotspotLost != null)
                    this.onHotspotLost.run();
            }
        };

        //핫스팟으로 와이파이 변경
        connectivityManager.requestNetwork(networkRequest, hotspotCallback);
    }

    /**
     * 이 메소드는 외부 네트워크에 연결하고 결과에 따라 다른 콜백함수를 호출한다.
     * @param ssid 외부 네트워크의 SSID
     * @param pw 외부 네트워크의 비밀번호
     */
    public void connectToExternal(String ssid, String pw) {
        //WifiNetworkSpecifier 객체 생성을 통해 와이파이 연결 수립을 정의함
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(pw)
                .build();

        //NetworkRequest에 위의 specifier객체를 념겨줌으로써 연결수립을 요청함
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build();

        //Activity의 시스템 서비스로부터 CONNECTIVITY_SERVICE를 가져와 ConnectivityManager 객체를 참조함
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback externalCallback = new ConnectivityManager.NetworkCallback() {
            private Runnable onExternalAvailable = WifiConnectionManager.this.onExternalAvailable;
            private Runnable onExternalUnAvailable = WifiConnectionManager.this.onExternalUnAvailable;
            private Runnable onExternalLost = WifiConnectionManager.this.onExternalLost;

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
                if (this.onExternalAvailable != null)
                    this.onExternalAvailable.run();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                if (this.onExternalUnAvailable != null)
                    this.onExternalUnAvailable.run();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (this.onExternalLost != null)
                    this.onExternalLost.run();
            }
        };

        //외부 네트워크로 와이파이 변경
        connectivityManager.requestNetwork(networkRequest, externalCallback);
    }

    /**
     * 이 메소드는 아두이노로 외부 네트워크 정보를 전송한다.
     * @param ssid 외부네트워크의 SSID
     * @param pw 외부네트워크의 비밀번호
     * @param connTimeout 연결 타임아웃(ms)
     * @param onSuccess 성공시 실행될 Runnable
     * @param onFailed 실패시 실행될 Runnable
     * @param onError 오류시 실행될 Runnable
     */
    public void sendExternalWifiInfo(String ssid, String pw, int connTimeout, Runnable onSuccess, Runnable onFailed, Runnable onError) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL("http://192.168.4.1:12344/regExtWifi?ssid=" + ssid + "&pw=" + pw);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(connTimeout);
                    connection.connect();

                    final int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) onSuccess.run();
                    else onFailed.run();

                } catch (IOException e) {
                    e.printStackTrace();
                    onError.run();
                }

            }
        }).start();
    }

    /**
     * 이 메소드는 아두이노의 Broadcast 메시지를 파싱하고 IP를 저장, 그 후 해당 IP를 타겟으로 삼아 ACK를 전송한다.
     * @param timeout UDP 프로세스 타임아웃(ms)
     * @return  성공시 true, 실패시 false
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
            socket = new DatagramSocket();
            packet = new DatagramPacket(sendData, sendData.length, destAddr, 12345);
            socket.send(packet);

            socket.close();

            this.onUdpSuccess.run();
            return true;
        } catch (SocketTimeoutException e) {  //타임아웃을 먼저 예외처리해야함\
            this.onUdpTimeout.run();
        } catch (IOException e) {
            System.out.println("UDP 오류");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 이 메소드는 최종연결을 확인하기 위해 아두이노의 웹서버에 접속한다.
     * @param connTimeout 최종연결 타임아웃(ms)
     * @return  성공시 true, 실패시 false
     */
    public boolean sendWebmessage(int connTimeout) {

        try {
            URL url = new URL("http://" + this.arduinoIP + ":12345/hello");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connTimeout);
            connection.connect();

            final int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) this.onSendWebMessageSuccess.run();
            else this.onSendWebMessageFailed.run();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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

    public void setOnSendWebMessageSuccess(Runnable runnable) {
        this.onSendWebMessageSuccess = runnable;
    }

    public void setOnSendWebMessageFailed(Runnable runnable) {
        this.onSendWebMessageFailed = runnable;
    }

}

