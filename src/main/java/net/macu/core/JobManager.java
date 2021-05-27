package net.macu.core;

import net.macu.UI.ViewManager;
import net.macu.cutter.Cutter;
import net.macu.disk.MultiScanSaver;
import net.macu.disk.ScanSaver;
import net.macu.disk.SingleScanSaver;
import net.macu.service.Service;
import net.macu.service.ServiceManager;
import net.macu.writer.ImgWriter;

import java.awt.image.BufferedImage;

public class JobManager {
    private Service service;
    private Cutter cutter;
    private ScanSaver saver;
    private boolean cancel = false;

    public boolean runJob(String url, Cutter cutter, boolean isReturnsSingleFile, String path, ImgWriter imgWriter,
                          ViewManager viewManager) {
        cancel = false;
        if (url != null)
            service = ServiceManager.getService(url);
        else
            service = null;
        if (service == null) {
            ViewManager.showMessageDialog("core.JobManager.runJob.unsupported_service", viewManager.getView());
            return false;
        }

        state = State.PARSING;
        BufferedImage[] fragments = service.parsePage(url, viewManager);
        if (cancel) {
            return false;
        }
        return runJob(fragments, cutter, isReturnsSingleFile, path, imgWriter, viewManager);
    }

    private State state = State.NO_JOB;

    public void cancel() {
        cancel = true;
        switch (state) {
            case NO_JOB:
                return;
            case PARSING:
                if (service != null)
                    service.cancel();
                return;
            case CUTTING:
                if (cutter != null)
                    cutter.cancel();
                return;
            case DROPPING_TO_DISK:
                if (saver != null)
                    saver.cancel();
        }
    }

    private enum State {
        NO_JOB, PARSING, CUTTING, DROPPING_TO_DISK
    }

    public boolean runJob(BufferedImage[] fragments, Cutter cutter, boolean isReturnsSingleFile, String path,
                          ImgWriter imgWriter, ViewManager viewManager) {
        cancel = false;
        if (fragments == null) return false;

        this.cutter = cutter;
        state = State.CUTTING;
        BufferedImage[] destImg = this.cutter.cutScans(fragments, viewManager);
        if (destImg == null) return false;

        saver = isReturnsSingleFile ? new SingleScanSaver(path) : new MultiScanSaver(path);
        state = State.DROPPING_TO_DISK;
        saver.saveToDisk(destImg, imgWriter, viewManager);

        state = State.NO_JOB;
        service = null;
        this.cutter = null;
        saver = null;
        System.gc();

        cancel = false;
        return true;
    }
}
