package net.macu.browser.proxy;

import java.awt.image.BufferedImage;

public interface ImageListener {
    void onImageCaptured(String url, BufferedImage image);
}
