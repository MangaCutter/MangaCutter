package net.macu.browser.image_proxy;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CapturedImageMap {
    private final HashMap<String, BufferedImage> capturedMap = new HashMap();

    public void putImage(String originUrl, BufferedImage image) {
        synchronized (capturedMap) {
            capturedMap.put(originUrl, image);
        }
        System.out.println("got " + originUrl);
//        BrowserPlugin.getPlugin().sendMessage("got " + originUrl);
    }

    public BufferedImage getImage(String originUrl) {
        synchronized (capturedMap) {
            return capturedMap.get(originUrl);
        }
    }

    public void clearDB() {
        capturedMap.clear();
    }
}
