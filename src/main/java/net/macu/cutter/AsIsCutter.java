package net.macu.cutter;

import net.macu.UI.ViewManager;

import java.awt.image.BufferedImage;

public class AsIsCutter implements Cutter {
    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        return fragments;
    }

    @Override
    public void cancel() {

    }
}
