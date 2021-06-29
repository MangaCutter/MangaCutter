package net.macu.browser.interceptor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Interceptor {
    private static final ArrayList<InterceptedImage> interceptedImages = new ArrayList();

    public static void removeImages(int identifier) {
        for (int i = 0; i < interceptedImages.size(); i++) {
            if (interceptedImages.get(i).browserId == identifier) {
                interceptedImages.remove(i);
                i--;
            }
        }
    }

    public static void addImage(int browserId, String url, BufferedImage image) {
        System.out.println(url);
        interceptedImages.add(new InterceptedImage(browserId, url, image));
    }

    public static BufferedImage getImage(String url) {
        for (InterceptedImage interceptedImage : interceptedImages) {
            if (interceptedImage.url.endsWith(url))
                return interceptedImage.image;
        }
        return null;
    }

    private static class InterceptedImage {
        private final long browserId;
        private final String url;
        private final BufferedImage image;

        private InterceptedImage(int browserId, String url, BufferedImage image) {
            this.browserId = browserId;
            this.url = url;
            this.image = image;
        }
    }
}
