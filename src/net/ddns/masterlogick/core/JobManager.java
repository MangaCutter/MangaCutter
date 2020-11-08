package net.ddns.masterlogick.core;

import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.cutter.Cutter;
import net.ddns.masterlogick.cutter.LongCutter;
import net.ddns.masterlogick.cutter.OneScanCutter;
import net.ddns.masterlogick.disk.OneScanSaver;
import net.ddns.masterlogick.disk.ScanSaver;
import net.ddns.masterlogick.downloader.Downloader;
import net.ddns.masterlogick.downloader.SimpleDownloader;
import net.ddns.masterlogick.service.Service;
import net.ddns.masterlogick.service.ServiceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JobManager {
    private static enum State {
        NO_JOB, PARSING, DOWNLOADING, CUTTING, DROPPING_TO_DISK
    }

    private static State state = State.NO_JOB;

    public static void cancel() {
        switch (state) {
            case NO_JOB:
                return;
            case PARSING:
                return;
            case DOWNLOADING:
                return;
            case CUTTING:
                return;
            case DROPPING_TO_DISK:
                return;
        }
    }

    public static void startJob(String uri, String out) {
        Service s = ServiceManager.getService(uri);
        if (s == null) {
            ViewManager.showMessage("Неправильная ссылка или скачка с данного сервиса не поддерживается.\n" +
                    "Полный список поддерживаемых сервисов есть в Справке");
            return;
        }

        state = State.PARSING;
        List<String> fragmentPathList = s.parsePage(uri);

        state = State.DOWNLOADING;
        Downloader downloader = new SimpleDownloader();
        BufferedImage[] fragments = downloader.downloadFragments(fragmentPathList);
        /*BufferedImage[] fragments = new BufferedImage[87];
        for (int i = 0; i < fragments.length; i++) {
            try {
                fragments[i]=ImageIO.read(new File("/home/user/test/" + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        state = State.CUTTING;
        Cutter cutter = new OneScanCutter();
//        Cutter cutter = new LongCutter();
        BufferedImage[] destImg = cutter.cutScans(fragments);

        state = State.DROPPING_TO_DISK;
        ScanSaver saver = new OneScanSaver();
        saver.saveToDisk(destImg, out);
    }
}
