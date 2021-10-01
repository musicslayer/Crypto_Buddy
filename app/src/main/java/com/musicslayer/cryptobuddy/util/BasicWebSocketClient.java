package com.musicslayer.cryptobuddy.util;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class BasicWebSocketClient extends WebSocketClient {
    public String RESULT = null;

    public BasicWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(String message) {
        RESULT = message;
    }

    @Override
    public void onError(Exception ex) {
        ExceptionLogger.processException(ex);
    }
}