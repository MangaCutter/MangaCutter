package net.macu.browser.plugin;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;

public class PluginWebSocketServer extends org.java_websocket.server.WebSocketServer {
    private WebSocket connectedSocket = null;

    public PluginWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        connectedSocket = webSocket;
        System.out.println("client connected");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        connectedSocket = null;
        System.out.println("client disconnected");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("server started");
    }

    public boolean isConnected() {
        return connectedSocket != null;
    }

    public WebSocket getConnectedSocket() {
        return connectedSocket;
    }
}
