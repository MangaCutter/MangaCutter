package net.ddns.masterlogick.disk;

import net.ddns.masterlogick.UI.ViewManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MultiScanSaver implements ScanSaver {
    private boolean cancel = false;

    @Override
    public void saveToDisk(BufferedImage[] images, String path) {
        ViewManager.startProgress(images.length, "Сброс на диск: 0/" + images.length);
        for (int i = 0; i < images.length; i++) {
            try {
                ImageIO.write(images[i], "PNG", new File(path + i + ".png"));
                ViewManager.startProgress(images.length, "Сброс на диск: " + (i + 1) + "/" + images.length);
                if (cancel) return;
            } catch (IOException e) {
                ViewManager.showMessage("Не удалось сохранить скан: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
