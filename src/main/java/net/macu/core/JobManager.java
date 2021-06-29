package net.macu.core;

import net.macu.UI.ViewManager;
import net.macu.browser.JCefLoader;
import net.macu.browser.OffscreenBrowser;
import net.macu.cutter.Cutter;
import net.macu.disk.MultiScanSaver;
import net.macu.disk.ScanSaver;
import net.macu.disk.SingleScanSaver;
import net.macu.imgWriter.ImgWriter;
import net.macu.service.Service;
import net.macu.service.ServiceManager;
import net.macu.settings.Settings;

import java.awt.image.BufferedImage;

public class JobManager {
    private Service service;
    private Cutter cutter;
    private ScanSaver saver;
    private boolean cancel = false;

    private State state = State.NO_JOB;

    /**
     * Common download method for all cases
     *
     * @param url         Path to source
     * @param cutter      Selected cutter
     * @param path        Output path
     * @param imgWriter   Selected ImgWriter
     * @param viewManager ViewManager instance for interaction with user
     * @return True - if download was completed successfully
     */
    public boolean runJob(String url, Cutter cutter, String path, ImgWriter imgWriter,
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

        BufferedImage[] fragments;

        Method method = Method.valueOf(Settings.JobManager_MethodPriority.getValue());
        if (service.supportsNativeDownloading() && method.equals(Method.Native)) {
            fragments = service.parsePage(url, viewManager);
        } else if (service.supportsBrowserDownloading() && method.equals(Method.Browser) && JCefLoader.isLoaded()) {
            fragments = OffscreenBrowser.executeScript(url, service.getBrowserInjectingScript());
        } else if (service.supportsNativeDownloading()) {
            fragments = service.parsePage(url, viewManager);
        } else if (service.supportsBrowserDownloading() && JCefLoader.isLoaded()) {
            fragments = OffscreenBrowser.executeScript(url, service.getBrowserInjectingScript());
        } else {
            fragments = null;
        }

        if (cancel) {
            return false;
        }

        if (fragments == null) return false;
        if (fragments.length == 0) return true;

        this.cutter = cutter;
        state = State.CUTTING;
        BufferedImage[] destImg = this.cutter.cutScans(fragments, viewManager);
        if (destImg == null) return false;
        if (destImg.length == 0) return true;

        if (cancel) {
            return false;
        }

        saver = this.cutter.isReturnsSingleFile() ? new SingleScanSaver(path) : new MultiScanSaver(path);
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

    public enum Method {
        Native, Browser
    }
}
