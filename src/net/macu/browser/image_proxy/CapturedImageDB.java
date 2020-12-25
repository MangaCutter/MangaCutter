package net.macu.browser.image_proxy;

import net.macu.browser.plugin.BrowserPlugin;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CapturedImageDB {
    private static final HashMap<String, BufferedImage> capturedList = new HashMap();

    public static void putImage(String originUrl, BufferedImage image) {
        synchronized (capturedList) {
            capturedList.put(originUrl, image);
        }
        BrowserPlugin.getPlugin().sendMessage("got " + originUrl);
    }

    public static BufferedImage getImage(String originUrl) {
        synchronized (capturedList) {
            return capturedList.get(originUrl);
        }
    }
}
