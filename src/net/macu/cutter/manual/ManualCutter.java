package net.macu.cutter.manual;

import net.macu.UI.ManualCutterFrame;
import net.macu.cutter.Cutter;

import java.awt.image.BufferedImage;

public class ManualCutter implements Cutter {
    ManualCutterFrame mcf;

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments) {
        mcf = new ManualCutterFrame(fragments);
        synchronized (mcf.getLocker()) {
            try {
                mcf.getLocker().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mcf.getResult();
    }

    @Override
    public void cancel() {
        if (mcf != null) {
            mcf.cancel();
        }
    }
}
