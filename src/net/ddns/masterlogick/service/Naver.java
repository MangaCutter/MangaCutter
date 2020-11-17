package net.ddns.masterlogick.service;

import net.ddns.masterlogick.UI.ViewManager;
import org.jsoup.Jsoup;

import javax.swing.*;
import java.net.URI;
import java.util.List;

public class Naver extends Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri) {
        ViewManager.startProgress(1, "Скачивание главной страницы");
        try {
            String sb = IOManager.sendRequest(uri);
            if (cancel) return null;
            ViewManager.incrementProgress("Скачана главная страница");
            return Jsoup.parse(sb).selectFirst("div.wt_viewer").select("img").eachAttr("src");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не удалось скачать главную страницу: " + e.getMessage());
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
