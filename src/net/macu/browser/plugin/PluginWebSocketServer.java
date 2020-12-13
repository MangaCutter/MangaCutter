package net.macu.browser.plugin;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class PluginWebSocketServer extends WebSocketServer {
    private final BrowserPlugin browserPlugin;
    private WebSocket pluginSocket;

    public PluginWebSocketServer(int port, BrowserPlugin plugin) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
        browserPlugin = plugin;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        if (pluginSocket != null) {
            pluginSocket.close();
        }
        pluginSocket = webSocket;
        System.out.println("client connected");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        pluginSocket = null;
        System.out.println("client disconnected");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        browserPlugin.onMessage(s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Plugin WebSocket server started on port " + getPort());
    }

    public boolean isConnected() {
        return pluginSocket != null && pluginSocket.isOpen();
    }

    public WebSocket getPLuginSocket() {
        return pluginSocket;
    }
}
