package net.ddns.masterlogick.downloader;

import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.core.IOManager;

import java.awt.image.BufferedImage;
import java.util.List;

public class SimpleDownloader implements Downloader {
    private boolean cancel = false;

    @Override
    public BufferedImage[] downloadFragments(List<String> paths) {
        ViewManager.startProgress(paths.size(), "Скачивание фрагментов: 0/" + paths.size());
        BufferedImage[] fragments = new BufferedImage[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            if (cancel) return null;
            try {
                fragments[i] = IOManager.downloadImage(paths.get(i));
                ViewManager.incrementProgress("Скачивание фрагментов: " + (i + 1) + "/" + paths.size());
            } catch (Exception e) {
                ViewManager.showMessage("Ошибка при скачивании фрагмента: " + e.toString() + "\n" + "Скачивание прервано");
                e.printStackTrace();
                return null;
            }
        }
        return fragments;
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
