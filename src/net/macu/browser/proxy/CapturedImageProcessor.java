package net.macu.browser.proxy;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Vector;

public class CapturedImageProcessor {
    private final HashMap<String, BufferedImage> capturedMap = new HashMap();
    private final int c = 0;
    private final Vector<ImageListener> listeners = new Vector<>();

    public void putImage(String originUrl, BufferedImage image) {
        synchronized (capturedMap) {
            capturedMap.put(originUrl, image);
        }
        try {
            listeners.forEach(listener -> listener.onImageCaptured(originUrl, image));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(String originUrl) {
        synchronized (capturedMap) {
            return capturedMap.get(originUrl);
        }
    }

    public void clearDB() {
        capturedMap.clear();
    }

    public void addImageListener(ImageListener listener) {
        listeners.add(listener);
    }

    public boolean removeImageListener(ImageListener listener) {
        return listeners.remove(listener);
    }
}
