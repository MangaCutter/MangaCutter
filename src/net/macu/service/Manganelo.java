/*
package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class Manganelo implements Service {

    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, "Скачивание главной страницы");
        try {
            String sb = IOManager.sendRequest(uri);
            if (cancel) return null;
            viewManager.incrementProgress("Скачана главная страница");
            return Jsoup.parse(sb).selectFirst("div.container-chapter-reader").select("img").eachAttr("src");
        } catch (IOException e) {
            viewManager.showMessageDialog("Не удалось скачать главную страницу: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("manganelo.com");
    }

    public String getInfo() {
        return "Manganelo: manganelo.com";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
*/
