package net.ddns.masterlogick.cutter.manual;

import net.ddns.masterlogick.cutter.Cutter;

import java.awt.image.BufferedImage;

public class ManualCutter implements Cutter {
    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments) {
        ManualCutterFrame mcf = new ManualCutterFrame(fragments);
        Object locker = mcf.getLocker();
        synchronized (locker) {
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

    }
}
