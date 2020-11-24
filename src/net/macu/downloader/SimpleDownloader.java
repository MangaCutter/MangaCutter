package net.macu.downloader;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.settings.L;

import java.awt.image.BufferedImage;
import java.util.List;

public class SimpleDownloader implements Downloader {
    private boolean cancel = false;

    @Override
    public BufferedImage[] downloadFragments(List<String> paths) {
        ViewManager.startProgress(paths.size(), L.get("downloader.SimpleDownloader.downloadFragments.progress", 0, paths.size()));
        BufferedImage[] fragments = new BufferedImage[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            if (cancel) return null;
            try {
                fragments[i] = IOManager.downloadImage(paths.get(i));
                ViewManager.incrementProgress(L.get("downloader.SimpleDownloader.downloadFragments.progress", i + 1, paths.size()));
            } catch (Exception e) {
                ViewManager.showMessageDialog(L.get("downloader.SimpleDownloader.downloadFragments.exception", e.toString()));
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
