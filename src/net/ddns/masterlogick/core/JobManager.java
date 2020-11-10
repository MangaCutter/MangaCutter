package net.ddns.masterlogick.core;

import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.cutter.Cutter;
import net.ddns.masterlogick.cutter.OneScanCutter;
import net.ddns.masterlogick.cutter.pasta.PastaCutter;
import net.ddns.masterlogick.disk.MultiScanSaver;
import net.ddns.masterlogick.disk.OneScanSaver;
import net.ddns.masterlogick.disk.ScanSaver;
import net.ddns.masterlogick.downloader.Downloader;
import net.ddns.masterlogick.downloader.SimpleDownloader;
import net.ddns.masterlogick.service.Service;
import net.ddns.masterlogick.service.ServiceManager;

import java.awt.image.BufferedImage;
import java.util.Arrays;
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

    public static boolean startJob(String uri, String out, String perfectSize, int selectedIndex, boolean cutOnGradient, int tolerance) {
        cancel = false;
        service = ServiceManager.getService(uri);
        if (service == null) {
            ViewManager.showMessage("Неправильная ссылка или скачивание с данного сервиса не поддерживается.\n" +
                    "Полный список поддерживаемых сервисов есть в Справке");
            return false;
        }
        int perfectHeight = -1;
        if (selectedIndex == ViewManager.MULTI_INDEX) {
            if (perfectSize.isEmpty()) {
                ViewManager.showMessage("Не указана высота!");
                return false;
            }
            try {
                perfectHeight = Integer.parseInt(perfectSize);
            } catch (NumberFormatException e) {
                ViewManager.showMessage("Неправильно указана высота: " + e.getMessage());
                return false;
            }
            if (perfectHeight < 0) {
                ViewManager.showMessage("Высота не может быть отрицательной!");
                return false;
            }
        }

        state = State.PARSING;
        List<String> fragmentPathList = service.parsePage(uri);
        if (cancel) {
            return false;
        }

        downloader = new SimpleDownloader();
        state = State.DOWNLOADING;
        BufferedImage[] fragments = downloader.downloadFragments(fragmentPathList);
        if (cancel) {
            return false;
        }
        fragmentPathList.clear();

        if (selectedIndex == ViewManager.MULTI_INDEX) {
            cutter = new PastaCutter(perfectHeight, cutOnGradient, tolerance);
            saver = new MultiScanSaver();
        } else if (selectedIndex == ViewManager.SINGLE_INDEX) {
            cutter = new OneScanCutter();
            saver = new OneScanSaver();
        } else if (selectedIndex == ViewManager.AS_IS_INDEX) {
            saver = new MultiScanSaver();
        }

        BufferedImage[] destImg;
        if (selectedIndex != ViewManager.AS_IS_INDEX) {
            state = State.CUTTING;
            destImg = cutter.cutScans(fragments);
            if (cancel) {
                return false;
            }
            Arrays.fill(fragments, null);
            System.gc();
        } else {
            destImg = fragments;
        }

        state = State.DROPPING_TO_DISK;
        saver.saveToDisk(destImg, out);

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
