package net.ddns.masterlogick.disk;

import net.ddns.masterlogick.UI.ViewManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SingleScanSaver implements ScanSaver {
    File out;

    public SingleScanSaver(String path) {
        out = new File(path);
    }

    @Override
    public void saveToDisk(BufferedImage[] images) {
        ViewManager.startProgress(1, "Сброс на диск: 0/1");
        try {
            ImageIO.write(images[0], "PNG", out);
        } catch (IOException e) {
            ViewManager.showMessageDialog("Не удалось сохранить скан: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
    }
}
