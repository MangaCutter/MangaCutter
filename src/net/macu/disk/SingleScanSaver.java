package net.macu.disk;

import net.macu.UI.ViewManager;
import net.macu.settings.L;

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
    public void saveToDisk(BufferedImage[] images, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("disc.SingleScanSaver.saveToDisk.progress"));
        try {
            ImageIO.write(images[0], "PNG", out);
        } catch (IOException e) {
            ViewManager.showMessageDialog(L.get("disc.SingleScanSaver.cant_save", e.toString()), viewManager.getView());
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
    }
}
