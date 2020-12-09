package net.macu.browser.image_proxy;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CapturedImageDB {
    private static final HashMap<String, BufferedImage> capturedList = new HashMap();

    public static void putImage(String originUrl, BufferedImage image) {
        synchronized (capturedList) {
            capturedList.put(originUrl, image);
        }
        System.out.println("captured image: " + originUrl + " " + image.toString());
    }

    public static BufferedImage getImage(String originUrl) {
        synchronized (capturedList) {
            return capturedList.get(originUrl);
        }
    }
}
