package net.macu.cutter;

import net.macu.UI.ViewManager;
import net.macu.settings.L;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SingleScanCutter implements Cutter {
    private boolean cancel = false;

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        viewManager.startProgress(fragments.length, L.get("cutter.SingleScanCutter.cutScans.progress", 0, fragments.length));
        int height = 0;
        int width = 0;
        for (BufferedImage fragment : fragments) {
            height += fragment.getHeight();
            width = Math.max(width, fragment.getWidth());
            if (cancel) return null;
        }
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = dst.getGraphics();
        int currentHeight = 0;

        for (int i = 0; i < fragments.length; i++) {
            if (cancel) return null;
            g.drawImage(fragments[i], 0, currentHeight, null);
            currentHeight += fragments[i].getHeight();
            viewManager.incrementProgress(L.get("cutter.SingleScanCutter.cutScans.progress", i + 1, fragments.length));
        }
        return new BufferedImage[]{dst};
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
