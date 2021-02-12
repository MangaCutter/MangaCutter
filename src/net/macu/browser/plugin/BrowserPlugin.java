package net.macu.browser.plugin;

import net.macu.UI.RequestFrame;
import net.macu.UI.ViewManager;
import net.macu.browser.proxy.CapturedImageProcessor;
import net.macu.browser.proxy.server.HTTPProxy;
import net.macu.settings.L;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.TreeMap;

public class BrowserPlugin {
    private static BrowserPlugin instance = null;
    private final PluginWebSocketServer webSocketServer;
    private final HTTPProxy httpProxy;
    private final CapturedImageProcessor capturedImages = new CapturedImageProcessor();
    private final TreeMap<String, RequestFrame> activeRequests = new TreeMap<>();

    public BrowserPlugin() {
        webSocketServer = new PluginWebSocketServer(50000, this);
        httpProxy = new HTTPProxy(50001, capturedImages);
    }

    public static BrowserPlugin getPlugin() {
        if (instance == null) instance = new BrowserPlugin();
        return instance;
    }

    public void start() {
        httpProxy.start();
        webSocketServer.start();
    }

    void onMessage(String message) {
        if (message.startsWith("alert ")) {
            ViewManager.showMessageDialog(L.get(message.substring(6)), null);
        } else if (message.startsWith("su ")) {
            try {
                JSONObject packet = (JSONObject) new JSONParser().parse(message.substring(message.indexOf(" ") + 1));
                String tabId = (String) packet.get("tabId");
                String tabUrl = (String) packet.get("url");
                JSONArray urls = (JSONArray) packet.get("data");
                RequestFrame f = activeRequests.get(tabId);
                if (f != null) {
                    f.reload(urls, tabUrl);
                } else {
                    activeRequests.put(tabId, new RequestFrame(urls, capturedImages, this, tabId, tabUrl));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (message.equals("cl")) {
            capturedImages.clearDB();
        } else if (message.startsWith("tma ")) {
            RequestFrame f = activeRequests.get(message.substring(4));
            if (f != null) f.onTooManyAttempts();
        } else if (message.startsWith("cancel ")) {
            RequestFrame f = activeRequests.remove(message.substring(7));
            if (f != null) f.onCancelRequest();
        }
    }

    public void onRequestCancel(String tabId) {
        activeRequests.remove(tabId);
        sendMessage("canceled " + tabId);
    }

    public void onRequestComplete(String tabId) {
        activeRequests.remove(tabId);
        sendMessage("completed " + tabId);
    }

    public void sendReloadMessage(String tabId) {
        sendMessage("refresh " + tabId);
    }

    public void sendMessage(String message) {
        if (webSocketServer.isConnected()) {
            webSocketServer.getPLuginSocket().send(message);
        } else {
            //todo in case of disconnection
        }
    }

    public boolean isConnected() {
        return webSocketServer.isConnected();
    }

    public boolean hasActiveRequests() {
        return !activeRequests.isEmpty();
    }
}
