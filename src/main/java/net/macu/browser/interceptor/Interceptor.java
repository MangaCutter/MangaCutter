package net.macu.browser.interceptor;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Interceptor {
    public static final HashMap<String, CompletableFuture<BufferedImage>> images = new HashMap<>();

    public static void addImage(String url, BufferedImage image) {
        System.out.println(url);
        getImageFuture(url).complete(image);
    }

    public static CompletableFuture<BufferedImage> getImageFuture(String url) {
        synchronized (images) {
            if (!images.containsKey(url))
                images.put(url, new CompletableFuture<>());
            return images.get(url);
        }
    }

    public static void removeImageRecord(String url) {
        synchronized (images) {
            images.remove(url);
        }
    }
}
