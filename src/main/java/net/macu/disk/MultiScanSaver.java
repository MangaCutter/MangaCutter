package net.macu.disk;

import net.macu.UI.ViewManager;
import net.macu.settings.L;
import net.macu.writer.ImgWriter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MultiScanSaver implements ScanSaver {
    private boolean cancel = false;
    String path;

    public MultiScanSaver(String path) {
        this.path = path;
    }

    @Override
    public void saveToDisk(BufferedImage[] images, ImgWriter imgWriter, ViewManager viewManager) {
        viewManager.startProgress(images.length, L.get("disk.MultiScanSaver.progress", 0, images.length));
        File directory = new File(path);
        try {
            if (directory.exists() && directory.isFile()) {
                ViewManager.showMessageDialog("disc.MultiScanSaver.file_exists", viewManager.getView(), path);
                return;
            }
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    ViewManager.showMessageDialog("disc.MultiScanSaver.cant_create", viewManager.getView(), path);
                }
            }
        } catch (SecurityException e) {
            ViewManager.showMessageDialog("disc.MultiScanSaver.cant_open", viewManager.getView(), path, e.toString());
            e.printStackTrace();
        }
        int filenameSize = Math.max(String.valueOf(images.length).length(), 3);
        String format = "%0" + filenameSize + "d";
        for (int i = 0; i < images.length; i++) {
            try {
                if (cancel) return;
                File f = new File(directory, String.format(format, (i + 1)) + ".png");
                if (f.exists()) {
                    if (!ViewManager.showConfirmDialog("disc.MultiScanSaver.confirm_rewrite", viewManager.getView(), f.getName())) {
                        continue;
                    }
                }
                imgWriter.writeImage(images[i], new FileOutputStream(f));

                images[i] = null;
                System.gc();

                viewManager.incrementProgress(L.get("disk.MultiScanSaver.progress", i + 1, images.length));
            } catch (IOException e) {
                ViewManager.showMessageDialog("disc.MultiScanSaver.cant_save", viewManager.getView(), e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
