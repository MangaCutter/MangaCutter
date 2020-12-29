package net.macu.browser.plugin;

import net.macu.browser.image_proxy.CapturedImageMap;
import net.macu.browser.image_proxy.proxy.HTTPProxy;
import net.macu.browser.image_proxy.proxy.HTTPSPipe;
import net.macu.browser.image_proxy.proxy.HTTPSProxy;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BrowserPlugin {
    private static BrowserPlugin instance = null;
    private final PluginWebSocketServer webSocketServer;
    private final HTTPProxy httpProxy;
    private final HTTPSProxy httpsProxy;
    private final CapturedImageMap capturedImages = new CapturedImageMap();

    private BrowserPlugin() {
        webSocketServer = new PluginWebSocketServer(50000, this);
        httpsProxy = new HTTPSProxy(50006, capturedImages);
        httpProxy = new HTTPProxy(50001, capturedImages, httpsProxy);
    }

    public static BrowserPlugin getPlugin() {
        if (instance == null) instance = new BrowserPlugin();
        return instance;
    }

    public void openUrlInDefaultBrowser(String url) {
        sendMessage("open " + url);
    }

    public void start() {
        httpsProxy.start();
        HTTPSPipe.startHandler();
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
        if (message.startsWith("alert ")) {
            System.out.println(message);
//            ViewManager.showMessageDialog(L.get(message.substring(6)));
        } else if (message.startsWith("su ")) {
            try {
                String tabId = message.substring(message.indexOf(" ") + 1, message.indexOf("\t"));
                JSONArray urls = (JSONArray) new JSONParser().parse(message.substring(message.indexOf("\t") + 1));
                final boolean[] capturedAll = {true};
                ArrayList<BufferedImage> images = new ArrayList<>();
                urls.forEach(url -> {
                    BufferedImage img = capturedImages.getImage((String) url);
                    if (!capturedAll[0] || img == null) {
                        capturedAll[0] = false;
                    } else {
                        images.add(img);
                    }
                });
                if (capturedAll[0]) {
                    sendMessage("done " + tabId);
                    for (int i = 0; i < images.size(); i++) {
                        try {
                            ImageIO.write(images.get(i), "PNG", new File("test/00" + i + ".png"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //todo invoke dialog window
                } else {
                    sendMessage("refresh " + tabId);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (message.equals("cl")) {
            capturedImages.clearDB();
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
