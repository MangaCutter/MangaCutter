package net.macu.cutter;

import net.macu.UI.ViewManager;
import net.macu.cutter.pasta.Frame;
import net.macu.settings.L;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ConstantHeightCutter implements Cutter {
    private final int height;
    private boolean cancel = false;

    public ConstantHeightCutter(int height) {
        this.height = height;
    }

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("cutter.ConstantHeightCutter.cutScans.progress"));
        ArrayList<Frame> result = new ArrayList<>();
        for (int currentOffset = 0; ; currentOffset += height) {
            if (cancel) return null;
            Frame f = new Frame(fragments, currentOffset, currentOffset + height - 1);
            if (f.getHeight() > 0) result.add(f);
            if (f.getHeight() != height) break;
        }
        return result.stream().map(Frame::createImage).toArray(BufferedImage[]::new);
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
