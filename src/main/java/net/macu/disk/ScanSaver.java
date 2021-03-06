package net.macu.disk;

import net.macu.UI.ViewManager;
import net.macu.imgWriter.ImgWriter;

import java.awt.image.BufferedImage;

public interface ScanSaver {

    void saveToDisk(BufferedImage[] images, ImgWriter imgWriter, ViewManager viewManager);

    void cancel();
}
