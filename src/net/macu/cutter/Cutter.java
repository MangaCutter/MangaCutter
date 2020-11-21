package net.macu.cutter;

import java.awt.image.BufferedImage;

public interface Cutter {
    BufferedImage[] cutScans(BufferedImage[] fragments);

    void cancel();
}
