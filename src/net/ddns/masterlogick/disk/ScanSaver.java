package net.ddns.masterlogick.disk;

import java.awt.image.BufferedImage;

public interface ScanSaver {
    void saveToDisk(BufferedImage[] images, String path);

    void cancel();
}
