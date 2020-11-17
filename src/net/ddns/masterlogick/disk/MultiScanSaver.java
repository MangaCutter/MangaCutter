package net.ddns.masterlogick.disk;

import net.ddns.masterlogick.UI.ViewManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MultiScanSaver implements ScanSaver {
    private boolean cancel = false;
    String path;

    public MultiScanSaver(String path) {
        this.path = path;
    }

    @Override
    public void saveToDisk(BufferedImage[] images) {
        ViewManager.startProgress(images.length, "Сброс на диск: 0/" + images.length);
        File directory = new File(path);
        try {
            if (directory.exists() && directory.isFile()) {
                ViewManager.showMessage(path + "\nПо указанному пути находится файл, а не папка.\nСохранение приостановлено.");
                return;
            }
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    ViewManager.showMessage(path + "\nНе удалось создать указанную папку.");
                }
            }
        } catch (SecurityException e) {
            ViewManager.showMessage(path + "\nНе удалось открыть указанную папку.\n" + e.getMessage());
            e.printStackTrace();
        }
        for (int i = 0; i < images.length; i++) {
            try {
                File f = new File(directory, String.format("%03d", (i + 1)) + ".png");
                if (f.exists()) {
                    if (JOptionPane.showConfirmDialog(null, "Файл " + f.getName() + " уже существует.\n"
                            + "Перезаписать?", "Внимание!", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                        continue;
                    }
                }
                ImageIO.write(images[i], "PNG", f);

                images[i] = null;
                System.gc();

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
