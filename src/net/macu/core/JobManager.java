package net.macu.core;

import net.macu.UI.ViewManager;
import net.macu.cutter.Cutter;
import net.macu.disk.ScanSaver;
import net.macu.downloader.Downloader;
import net.macu.downloader.SimpleDownloader;
import net.macu.service.Service;
import net.macu.service.ServiceManager;
import org.apache.http.client.methods.HttpUriRequest;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

public class JobManager {
    //todo add locker for job
    private static Service service;
    private static Downloader downloader;
    private static Cutter cutter;
    private static ScanSaver saver;
    private static boolean cancel = false;

    private enum State {
        NO_JOB, PARSING, DOWNLOADING, CUTTING, DROPPING_TO_DISK
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

    public synchronized boolean runJob(String url, Pipeline pipeline, ViewManager viewManager) {
        cancel = false;
        URI uri = URI.create(url);
        if (uri.getHost() != null && uri.getPath() != null)
            service = ServiceManager.getService(url);
        else
            service = null;
        if (service == null) {
            ViewManager.showMessageDialog("core.JobManager.runJob.unsupported_service", viewManager.getView());
            return false;
        }

        state = State.PARSING;
        List<HttpUriRequest> fragmentPathList = service.parsePage(url, viewManager);
        if (cancel) {
            return false;
        }
        if (fragmentPathList == null) return false;

        downloader = new SimpleDownloader();
        state = State.DOWNLOADING;
        BufferedImage[] fragments = downloader.downloadFragments(fragmentPathList, viewManager);
        if (cancel) {
            return false;
        }
        return runJob(fragments, pipeline, viewManager);
    }

    public synchronized boolean runJob(BufferedImage[] fragments, Pipeline pipeline, ViewManager viewManager) {
        cancel = false;
        if (fragments == null) return false;

        cutter = pipeline.cutter;
        state = State.CUTTING;
        BufferedImage[] destImg = pipeline.cutter.cutScans(fragments, viewManager);
        if (destImg == null) return false;

        saver = pipeline.saver;
        state = State.DROPPING_TO_DISK;
        pipeline.saver.saveToDisk(destImg, viewManager);

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
