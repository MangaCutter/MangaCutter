package net.macu.disk;

import net.macu.UI.ViewManager;

import java.awt.image.BufferedImage;

public interface ScanSaver {

    void saveToDisk(BufferedImage[] images, ViewManager viewManager);

    void cancel();
}
