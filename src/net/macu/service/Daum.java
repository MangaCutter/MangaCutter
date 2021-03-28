package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.settings.L;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Daum implements Service {
    private boolean cancel = false;

    @Override
    public List<HttpUriRequest> parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.Daum.parsePage.progress"));
        String viewerImagesUrl = "http://webtoon.daum.net/data/pc/webtoon/viewer_images/" + uri.substring(uri.lastIndexOf("/") + 1);
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(IOManager.sendRequest(viewerImagesUrl));
            JSONArray data = (JSONArray) json.get("data");
            String[] list = new String[data.size()];
            data.forEach(o -> list[((int) ((long) ((JSONObject) o).get("imageOrder"))) - 1] = (String) ((JSONObject) o).get("url"));
            ArrayList<HttpUriRequest> requests = new ArrayList<>(data.size());
            for (int i = 0; i < list.length; i++) {
                requests.add(new HttpGet(list[i]));
            }
            return requests;
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
