/*
package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Mangafreak implements Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri) {
        ViewManager.startProgress(1, "Скачивание главной страницы");
        try {
            System.out.println(IOManager.sendRequest(uri));
            String[] sb = IOManager.sendRequest(uri).split("\n");
            if (cancel) return null;
            ViewManager.incrementProgress("Скачана главная страница");
            ArrayList<String> pages = new ArrayList<>();
            for (int i = 0; i < sb.length; i++) {
                if (sb[i].startsWith("<img id=\"gohere\" src=\"")) {
                    String s = sb[i].substring("<img id=\"gohere\" src=\"".length());
                    pages.add(s.substring(0, s.indexOf("\"")));
                }
            }
            return pages;
        } catch (IOException e) {
            ViewManager.showMessageDialog("Не удалось скачать главную страницу: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("w11.mangafreak.net");
    }

    public String getInfo() {
        return "Mangafreak: w11.mangafreak.net";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
*/
