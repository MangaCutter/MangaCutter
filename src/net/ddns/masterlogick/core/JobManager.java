package net.ddns.masterlogick.core;

import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.cutter.Cutter;
import net.ddns.masterlogick.cutter.OneScanCutter;
import net.ddns.masterlogick.cutter.PastaCutter;
import net.ddns.masterlogick.disk.MultiScanSaver;
import net.ddns.masterlogick.disk.OneScanSaver;
import net.ddns.masterlogick.disk.ScanSaver;
import net.ddns.masterlogick.downloader.Downloader;
import net.ddns.masterlogick.downloader.SimpleDownloader;
import net.ddns.masterlogick.service.Service;
import net.ddns.masterlogick.service.ServiceManager;

import java.awt.image.BufferedImage;
import java.util.List;

public class JobManager {
    private static Service s;
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
                s.cancel();
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

    public static boolean startJob(String uri, String out, String prefix, String perfectSize, int selectedIndex) {
        cancel = false;
        s = ServiceManager.getService(uri);
        if (s == null) {
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
            if(perfectHeight<0){
                ViewManager.showMessage("Высота не может быть отрицательной!");
                return false;
            }
        }

        state = State.PARSING;
        List<String> fragmentPathList = s.parsePage(uri);
        if (cancel) {
            return false;
        }

        downloader = new SimpleDownloader();
        state = State.DOWNLOADING;
        BufferedImage[] fragments = downloader.downloadFragments(fragmentPathList);
        if (cancel) {
            return false;
        }

        if (!perfectSize.isEmpty()) {
            cutter = new PastaCutter(perfectHeight);
            saver = new MultiScanSaver(prefix);
        } else {
            cutter = new OneScanCutter();
            saver = new OneScanSaver();
        }

        state = State.CUTTING;
        BufferedImage[] destImg = cutter.cutScans(fragments);
        if (cancel) {
            return false;
        }

        state = State.DROPPING_TO_DISK;
        saver.saveToDisk(destImg, out);

        cancel = false;
        return true;
    }
}
