package net.macu.core;

import net.macu.UI.ViewManager;
import net.macu.cutter.Cutter;
import net.macu.disk.ScanSaver;
import net.macu.downloader.Downloader;
import net.macu.downloader.SimpleDownloader;
import net.macu.service.Service;
import net.macu.service.ServiceManager;
import net.macu.settings.L;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

public class JobManager {
    private static Service service;
    private static Downloader downloader;
    private static Cutter cutter;
    private static ScanSaver saver;
    private static boolean cancel = false;

    private enum State {
        NO_JOB, PARSING, DOWNLOADING, CUTTING, DROPPING_TO_DISK
    }

    private static State state = State.NO_JOB;

    public static void cancel() {
        cancel = true;
        switch (state) {
            case NO_JOB:
                return;
            case PARSING:
                if (service != null)
                    service.cancel();
                return;
            case DOWNLOADING:
                if (downloader != null)
                    downloader.cancel();
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

    public static synchronized boolean runJob(String url, Pipeline pipeline) {
        cancel = false;
        URI uri = URI.create(url);
        if (uri.getHost() != null && uri.getPath() != null)
            service = ServiceManager.getService(url);
        else
            service = null;
        if (service == null) {
            ViewManager.showMessageDialog(L.get("core.JobManager.runJob.unsupported_service"));
            return false;
        }

        state = State.PARSING;
        List<String> fragmentPathList = service.parsePage(url);
        if (cancel) {
            return false;
        }
        if (fragmentPathList == null) return false;

        downloader = new SimpleDownloader();
        state = State.DOWNLOADING;
        BufferedImage[] fragments = downloader.downloadFragments(fragmentPathList);
        if (cancel) {
            return false;
        }
        if (fragments == null) return false;

        cutter = pipeline.cutter;
        state = State.CUTTING;
        BufferedImage[] destImg = pipeline.cutter.cutScans(fragments);
        if (destImg == null) return false;

        saver = pipeline.saver;
        state = State.DROPPING_TO_DISK;
        pipeline.saver.saveToDisk(destImg);

        state = State.NO_JOB;
        service = null;
        downloader = null;
        cutter = null;
        saver = null;
        System.gc();

        cancel = false;
        return true;
    }
}
