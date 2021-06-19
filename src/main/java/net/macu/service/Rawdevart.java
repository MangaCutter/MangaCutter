package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.downloader.SimpleDownloader;
import net.macu.settings.L;
import org.jsoup.Jsoup;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

public class Rawdevart implements Service {
    private boolean cancel = false;

    @Override
    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("rawdevart.com");
    }

    @Override
    public boolean supportsNativeDownloading() {
        return true;
    }

    @Override
    public BufferedImage[] parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.Rawdevart.parsePage.progress"));
        try {
            String sb = IOManager.sendRequest(uri);
            if (cancel) return null;
            return SimpleDownloader.downloadFragments(
                    Jsoup.parse(sb).select("img.img-fluid.not-lazy").eachAttr("data-src"),
                    viewManager);

        } catch (IOException e) {
            ViewManager.showMessageDialog("service.Rawdevart.parsePage.io_exception",
                    viewManager.getView(), e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean supportsBrowserDownloading() {
        return false;
    }

    @Override
    public String getBrowserInjectingScript() {
        return null;
    }

    @Override
    public String getInfo() {
        return "Rawdevart: rawdevart.com";
    }

    @Override
    public void cancel() {
        cancel = true;
        SimpleDownloader.cancel();
    }
}
