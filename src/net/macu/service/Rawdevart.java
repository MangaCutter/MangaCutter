package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class Rawdevart extends Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri) {
        ViewManager.startProgress(1, "Скачивание главной страницы");
        try {
            String sb = IOManager.sendRequest(uri);
            if (cancel) return null;
            ViewManager.incrementProgress("Скачана главная страница");
            return Jsoup.parse(sb).select("img.img-fluid.not-lazy").eachAttr("data-src");
        } catch (IOException e) {
            ViewManager.showMessageDialog("Не удалось скачать главную страницу: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("rawdevart.com");
    }

    public String getInfo() {
        return "Rawdevart: rawdevart.com";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
