package net.ddns.masterlogick.core;

import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.cutter.Cutter;
import net.ddns.masterlogick.disk.ScanSaver;
import net.ddns.masterlogick.downloader.Downloader;
import net.ddns.masterlogick.downloader.SimpleDownloader;
import net.ddns.masterlogick.service.Service;
import net.ddns.masterlogick.service.ServiceManager;

import java.awt.image.BufferedImage;
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
                service.cancel();
                return;
            case DOWNLOADING:
                downloader.cancel();
                return;
            case CUTTING:
                cutter.cancel();
                return;
            case DROPPING_TO_DISK:
                saver.cancel();
        }
    }

    public static synchronized boolean runJob(String url, Pipeline pipeline) {
        cancel = false;
        service = ServiceManager.getService(url);
        if (service == null) {
            ViewManager.showMessage("Неправильная ссылка или скачивание с данного сервиса не поддерживается.\n" +
                    "Полный список поддерживаемых сервисов есть в Справке");
            return false;
        }

        state = State.PARSING;
        List<String> fragmentPathList = service.parsePage(url);
        if (cancel) {
            return false;
        }

        downloader = new SimpleDownloader();
        state = State.DOWNLOADING;
        BufferedImage[] fragments = downloader.downloadFragments(fragmentPathList);
        if (cancel) {
            return false;
        }

        state = State.CUTTING;
        BufferedImage[] destImg = pipeline.cutter.cutScans(fragments);

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
