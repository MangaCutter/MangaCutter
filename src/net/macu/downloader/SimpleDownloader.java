package net.macu.downloader;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.settings.L;
import org.apache.http.client.methods.HttpUriRequest;

import java.awt.image.BufferedImage;
import java.util.List;

public class SimpleDownloader implements Downloader {
    private boolean cancel = false;

    @Override
    public BufferedImage[] downloadFragments(List<HttpUriRequest> requests, ViewManager viewManager) {
        viewManager.startProgress(requests.size(), L.get("downloader.SimpleDownloader.downloadFragments.progress", 0, requests.size()));
        BufferedImage[] fragments = new BufferedImage[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            if (cancel) return null;
            try {
                fragments[i] = IOManager.downloadImage(requests.get(i));
                viewManager.incrementProgress(L.get("downloader.SimpleDownloader.downloadFragments.progress", i + 1, requests.size()));
            } catch (Exception e) {
                ViewManager.showMessageDialog("downloader.SimpleDownloader.downloadFragments.exception", viewManager.getView(), e.toString());
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
