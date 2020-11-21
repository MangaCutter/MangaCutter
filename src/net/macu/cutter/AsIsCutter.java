package net.macu.cutter;

import java.awt.image.BufferedImage;

public class AsIsCutter implements Cutter {
    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments) {
        return fragments;
    }

    @Override
    public void cancel() {

    }
}
