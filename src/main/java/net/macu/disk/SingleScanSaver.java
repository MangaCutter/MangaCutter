package net.macu.disk;

import net.macu.UI.ViewManager;
import net.macu.settings.L;
import net.macu.writer.ImgWriter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SingleScanSaver implements ScanSaver {
    File out;

    public SingleScanSaver(String path) {
        out = new File(path);
    }

    @Override
    public void saveToDisk(BufferedImage[] images, ImgWriter imgWriter, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("disc.SingleScanSaver.saveToDisk.progress"));
        try {
            imgWriter.writeImage(images[0], new FileOutputStream(out));
        } catch (IOException e) {
            ViewManager.showMessageDialog("disc.SingleScanSaver.cant_save", viewManager.getView(), e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
    }
}
