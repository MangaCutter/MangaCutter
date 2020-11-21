package net.macu.core;

import net.macu.cutter.Cutter;
import net.macu.disk.ScanSaver;

public class Pipeline {
    public final Cutter cutter;
    public final ScanSaver saver;

    public Pipeline(Cutter cutter, ScanSaver saver) {
        this.cutter = cutter;
        this.saver = saver;
    }
}
