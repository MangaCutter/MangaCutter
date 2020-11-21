package net.macu.disk;

import java.awt.image.BufferedImage;

public interface ScanSaver {
    void saveToDisk(BufferedImage[] images);

    void cancel();
}
