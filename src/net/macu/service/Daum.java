package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.settings.L;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class Daum implements Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri) {
        ViewManager.startProgress(1, L.get("service.Daum.parsePage.progress"));
        String viewerImagesUrl = "http://webtoon.daum.net/data/pc/webtoon/viewer_images/" + uri.substring(uri.lastIndexOf("/") + 1);
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(IOManager.sendRequest(viewerImagesUrl));
            JSONArray data = (JSONArray) json.get("data");
            String[] list = new String[data.size()];
            data.forEach(o -> list[((int) ((long) ((JSONObject) o).get("imageOrder"))) - 1] = (String) ((JSONObject) o).get("url"));
            return Arrays.asList(list);
        } catch (ParseException e) {
            ViewManager.showMessageDialog(L.get("service.Daum.parsePage.parse_exception", e.toString()));
            e.printStackTrace();
        } catch (IOException e) {
            ViewManager.showMessageDialog(L.get("service.Daum.parsePage.io_exception", e.toString()));
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
