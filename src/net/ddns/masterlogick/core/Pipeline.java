package net.ddns.masterlogick.core;

import net.ddns.masterlogick.cutter.Cutter;
import net.ddns.masterlogick.disk.ScanSaver;

public class Pipeline {
    public final Cutter cutter;
    public final ScanSaver saver;

    public Pipeline(Cutter cutter, ScanSaver saver) {
        this.cutter = cutter;
        this.saver = saver;
    }
}
