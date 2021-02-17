package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.settings.L;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class Naver implements Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.Naver.parsePage.progress"));
        try {
            String sb = IOManager.sendRequest(uri);
            if (cancel) return null;
            return Jsoup.parse(sb).selectFirst("div.wt_viewer").select("img").eachAttr("src");
        } catch (IOException e) {
            ViewManager.showMessageDialog("service.Naver.parsePage.io_exception", viewManager.getView(), e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("comic.naver.com");
    }

    public String getInfo() {
        return "Korean Naver: comic.naver.com";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
