package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.downloader.SimpleDownloader;
import net.macu.settings.L;
import org.jsoup.Jsoup;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

public class Naver implements Service {
    private boolean cancel = false;

    @Override
    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("comic.naver.com");
    }

    @Override
    public boolean supportsNativeDownloading() {
        return true;
    }

    @Override
    public BufferedImage[] parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.Naver.parsePage.progress"));
        try {
            String sb = IOManager.sendRequest(uri);
            if (cancel) return null;
            return SimpleDownloader.downloadFragments(
                    Jsoup.parse(sb).selectFirst("div.wt_viewer").select("img").eachAttr("src"),
                    viewManager);
        } catch (IOException e) {
            ViewManager.showMessageDialog("service.Naver.parsePage.io_exception", viewManager.getView(), e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean supportsBrowserDownloading() {
        return true;
    }

    @Override
    public String getBrowserInjectingScript() {
        return "() => Array.from(document.getElementsByClassName(\"wt_viewer\")[0].querySelectorAll(\"img\")).map(x=>x.src)";
    }

    @Override
    public String getInfo() {
        return "Korean Naver: comic.naver.com";
    }

    @Override
    public void cancel() {
        cancel = true;
        SimpleDownloader.cancel();
    }
}
