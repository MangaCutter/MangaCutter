package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.downloader.SimpleDownloader;
import net.macu.settings.L;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

public class Daum implements Service {
    private boolean cancel = false;

    @Override
    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("webtoon.daum.net");
    }

    @Override
    public boolean supportsNativeDownloading() {
        return true;
    }

    @Override
    public BufferedImage[] parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.Daum.parsePage.progress"));
        String viewerImagesUrl = "http://webtoon.daum.net/data/pc/webtoon/viewer_images/" + uri.substring(uri.lastIndexOf("/") + 1);
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(IOManager.sendRequest(viewerImagesUrl));
            JSONArray data = (JSONArray) json.get("data");
            String[] list = new String[data.size()];
            data.forEach(o -> list[((int) ((long) ((JSONObject) o).get("imageOrder"))) - 1] = (String) ((JSONObject) o).get("url"));
            return SimpleDownloader.downloadFragments(Arrays.asList(list), viewManager);
        } catch (ParseException e) {
            ViewManager.showMessageDialog("service.Daum.parsePage.parse_exception",
                    viewManager.getView(), e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            ViewManager.showMessageDialog("service.Daum.parsePage.io_exception",
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
        return "Webtoon Daum: webtoon.daum.net";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
