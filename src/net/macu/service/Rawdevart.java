package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.settings.L;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class Rawdevart implements Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.Rawdevart.parsePage.progress"));
        try {
            String sb = IOManager.sendRequest(uri);
            if (cancel) return null;
            return Jsoup.parse(sb).select("img.img-fluid.not-lazy").eachAttr("data-src");
        } catch (IOException e) {
            ViewManager.showMessageDialog(L.get("service.Rawdevart.parsePage.io_exception", e.toString()),
                    viewManager.getView());
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
