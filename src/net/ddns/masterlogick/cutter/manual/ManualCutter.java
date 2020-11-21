package net.ddns.masterlogick.cutter.manual;

import net.ddns.masterlogick.cutter.Cutter;

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
