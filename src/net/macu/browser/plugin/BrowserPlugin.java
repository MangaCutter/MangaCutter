package net.macu.browser.plugin;

import net.macu.browser.image_proxy.proxy.HTTPProxy;
import net.macu.browser.image_proxy.proxy.HTTPSPipe;
import net.macu.browser.image_proxy.proxy.HTTPSProxy;

public class BrowserPlugin {
    private static BrowserPlugin instance = null;
    private final PluginWebSocketServer webSocketServer;
    private final HTTPProxy httpProxy;
    private final HTTPSProxy httpsProxy;

    private BrowserPlugin() {
        webSocketServer = new PluginWebSocketServer(50000, this);
        httpProxy = new HTTPProxy(50001);
        httpsProxy = new HTTPSProxy(50002);
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
        httpsProxy.start();
        httpProxy.start();
        webSocketServer.start();
        HTTPSPipe.startHandler();
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
        } else System.out.println(message);
    }
}
