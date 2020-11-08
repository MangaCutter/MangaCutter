package net.ddns.masterlogick.disk;

import net.ddns.masterlogick.UI.ViewManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MultiScanSaver implements ScanSaver {
    private boolean cancel = false;
    private String prefix;

    public MultiScanSaver(String prefix) {
        this.prefix = prefix + "-";
    }

    @Override
    public void saveToDisk(BufferedImage[] images, String path) {
        ViewManager.startProgress(images.length, "Сброс на диск: 0/" + images.length);
        for (int i = 0; i < images.length; i++) {
            try {
                File f = new File(path + File.separator + prefix + i + ".png");
                if (f.exists()) {
                    if (JOptionPane.showConfirmDialog(null, "Файл " + f.getName() + " уже существует.\n"
                            + "Перезаписать?", "Внимание!", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                        continue;
                    }
                }
                ImageIO.write(images[i], "PNG", f);
                ViewManager.incrementProgress("Сброс на диск: " + (i + 1) + "/" + images.length);
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
