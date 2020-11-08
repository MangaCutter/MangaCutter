package net.ddns.masterlogick.disk;

import net.ddns.masterlogick.UI.ViewManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OneScanSaver implements ScanSaver {
    private boolean cancel = false;

    @Override
    public void saveToDisk(BufferedImage[] images, String path) {
        ViewManager.startProgress(1, "Сброс на диск: 0/1");
        try {
            ImageIO.write(images[0], "PNG", new File(path));
        } catch (IOException e) {
            ViewManager.showMessage("Не удалось сохранить скан: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
    }
}
