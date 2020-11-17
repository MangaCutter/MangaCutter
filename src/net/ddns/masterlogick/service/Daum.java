package net.ddns.masterlogick.service;

import net.ddns.masterlogick.UI.ViewManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class Daum extends Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri) {
        ViewManager.startProgress(1, "Скачивание главной страницы");
        String viewerImagesUrl = "http://webtoon.daum.net/data/pc/webtoon/viewer_images/" + uri.substring(uri.lastIndexOf("/") + 1);
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(IOManager.sendRequest(viewerImagesUrl));
            JSONArray data = (JSONArray) json.get("data");
            String[] list = new String[data.size()];
            data.forEach(o -> list[((int) ((long) ((JSONObject) o).get("imageOrder"))) - 1] = (String) ((JSONObject) o).get("url"));
            return Arrays.asList(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("webtoon.daum.net");
    }

    public String getInfo() {
        return "Webtoon Daum: webtoon.daum.net";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
