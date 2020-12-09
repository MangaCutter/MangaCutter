package net.macu.browser.plugin;

public class Plugin {
    private static PluginWebSocketServer server;

    public static void initServer() {
        server = new PluginWebSocketServer(50000);
        server.start();
    }

    public static void openUrlInDefaultBrowser(String url) {
        if (server.isConnected()) {
            server.getConnectedSocket().send("open " + url);
        }
    }

    public static void waitUntilConnected(int waitForSecs) {
        for (int i = 0; i < waitForSecs && !server.isConnected(); i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
