package net.macu.browser.plugin;

import net.macu.browser.image_proxy.proxy.HTTPProxy;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class BrowserPlugin {
    private static BrowserPlugin instance = null;
    private final PluginWebSocketServer webSocketServer;
    private final HTTPProxy httpProxy;

    private BrowserPlugin() {
        webSocketServer = new PluginWebSocketServer(50000, this);
        httpProxy = new HTTPProxy(50001);
    }

    public static BrowserPlugin getPlugin() {
        if (instance == null) instance = new BrowserPlugin();
        return instance;
    }

    public void openUrlInDefaultBrowser(String url) {
        if (webSocketServer.isConnected()) {
            webSocketServer.getPLuginSocket().send("open " + url);
        }
    }

    public void start() {
        httpProxy.start();
        webSocketServer.start();
    }

    public boolean waitUntilConnected(int timeout) {
        for (int i = 0; i < timeout && !webSocketServer.isConnected(); i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return webSocketServer.isConnected();
    }

    void onMessage(String message) {
        if (message.equals("ps")) {
            webSocketServer.getPLuginSocket().send("ps " + httpProxy.isStarted());
        } else if (message.startsWith("dc ")) {
            try {
                ArrayList data = (JSONArray) new JSONParser().parse(message.substring(3));
                for (int i = 0; i < data.size(); i++) {
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else System.out.println(message);
    }
}
