package net.macu.browser.plugin;

import net.macu.UI.ViewManager;
import net.macu.browser.image_proxy.proxy.HTTPProxy;
import net.macu.settings.L;

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
        sendMessage("open " + url);
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
        } else if (message.startsWith("alert ")) {
            ViewManager.showMessageDialog(L.get(message.substring(6)));
        } else System.out.println(message);
    }

    public void sendMessage(String message) {
        if (webSocketServer.isConnected()) {
            webSocketServer.getPLuginSocket().send(message);
        } else {
            //todo in case of disconnection
        }
    }
}
