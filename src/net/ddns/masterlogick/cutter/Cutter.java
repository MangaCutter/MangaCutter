package net.ddns.masterlogick.cutter;

import java.awt.image.BufferedImage;

public interface Cutter {
    BufferedImage[] cutScans(BufferedImage[] fragments);

    void cancel();
}
