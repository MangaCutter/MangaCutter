package net.macu.cutter.manual;

import net.macu.UI.ManualCutterFrame;
import net.macu.UI.ViewManager;
import net.macu.cutter.Cutter;

import java.awt.image.BufferedImage;

public class ManualCutter implements Cutter {
    ManualCutterFrame mcf;

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        mcf = new ManualCutterFrame(fragments);
        return mcf.waitForResults();
    }

    @Override
    public void cancel() {
        if (mcf != null) {
            mcf.cancel();
        }
    }
}
