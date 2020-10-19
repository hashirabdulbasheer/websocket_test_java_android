package com.example.websockettest;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WebSocket mWs = null;

    // server url
    private static final String URL = "wss://deliverycommand.com/live/websocket";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }


        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);

        try {
            String serverName = Uri.parse(URL).getHost();
            factory.setServerName(serverName);
            mWs = factory.createSocket(URL);
            Socket socket = mWs.getSocket();

            Log.e("SOCKET", "Enabling SNI for " + serverName);
            Log.e("SOCKET", "ANDROID VERSION:   " + Build.VERSION.SDK_INT);

            try {
                Method method = socket.getClass().getMethod("setHostname", String.class);
                method.invoke(socket, serverName);
            } catch (Exception e) {
                Log.e("SOCKET", "SNI configuration failed", e);
            }

            mWs.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    Log.e("SOCKET", "onTextMessage: " + message);
                }

                @Override
                public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                    Log.e("SOCKET", "State Changed : -> " + newState.name());
                }


                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    Log.e("SOCKET", "on Connected : " + headers.toString());
                }


                @Override
                public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                    Log.e("SOCKET", "on Connected ERROR : " + exception.getLocalizedMessage());
                    exception.printStackTrace();
                }


                @Override
                public void onDisconnected(WebSocket websocket,
                                           WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                           boolean closedByServer) throws Exception {
                    Log.e("SOCKET", "on Disconnected");
                }
            });

            mWs.connectAsynchronously();

        } catch (IOException e) {
            Log.e("SOCKET", " " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        final Button sendMessageB = (Button) findViewById(R.id.sendMessageB);
        sendMessageB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("SOCKET", "Trying to Send Message...");
                sendMessage();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mWs != null) {
            mWs.disconnect();
            mWs = null;
        }
    }

    public void sendMessage() {
        if (mWs.isOpen()) {
            mWs.sendText("Message from Android!");
            Log.e("SOCKET", "Sending Message...");
        } else {
            Log.e("SOCKET", "ERROR: Socket not open");
        }
    }
}
