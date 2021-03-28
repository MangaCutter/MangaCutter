package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.settings.L;
import net.macu.util.JSEngine;
import net.macu.util.LZString;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.EvaluatorException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ManhuaGui implements Service {
    private boolean cancel = false;

    @Override
    public List<HttpUriRequest> parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.AcQq.parsePage.progress"));
        try {
            String jsonData = "";
            String html = IOManager.sendRequest(uri);
            String[] parts = html.split("<script type=\"text/javascript\">");
            if (parts.length != 3) {
                ViewManager.showMessageDialog("service.ManhuaGui.parsePage.unknown_format", viewManager.getView());
                return null;
            }
            String keyEval = parts[2].substring(parts[2].indexOf("("), parts[2].indexOf("</script>")).trim();
            keyEval = keyEval.substring(keyEval.indexOf("("));
            String preEncodedData = keyEval.substring(0, keyEval.lastIndexOf("['\\x73\\x70\\x6c\\x69\\x63']"));
            preEncodedData = preEncodedData.substring(0, preEncodedData.lastIndexOf("'"));
            preEncodedData = preEncodedData.substring(preEncodedData.lastIndexOf("'") + 1);
            String decodedPreEncodedData = LZString.decompressFromBase64(preEncodedData);
            keyEval = keyEval.replace(preEncodedData, decodedPreEncodedData);
            keyEval = keyEval.replace("['\\x73\\x70\\x6c\\x69\\x63']", ".split");
            try {
                jsonData = (String) JSEngine.evaluate(keyEval);
            } catch (EvaluatorException e) {
                e.printStackTrace();
                ViewManager.showMessageDialog("service.ManhuaGui.parsePage.parse_exception", viewManager.getView(), e.toString());
                return null;
            }
            jsonData = jsonData.substring(jsonData.indexOf("{"), jsonData.lastIndexOf("}") + 1);
            JSONObject json = null;
            try {
                json = (JSONObject) (new JSONParser().parse(jsonData));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ArrayList<String> files = (ArrayList<String>) json.get("files");
            String path = "https://i.hamreus.com" + json.get("path");
            final String[] suffix = {"?"};
            JSONObject keys = ((JSONObject) json.get("sl"));
            keys.forEach((o, o2) -> suffix[0] += (o + "=" + o2 + "&"));
            if (!keys.isEmpty())
                suffix[0] = suffix[0].substring(0, suffix[0].length() - 1);
            ArrayList<HttpUriRequest> requests = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                HttpGet r = new HttpGet(path + files.get(i) + suffix[0]);
                r.addHeader("Referer", "http://" + URI.create(uri).getHost() + "/");
                requests.add(r);
            }
            return requests;
        } catch (IOException e) {
            e.printStackTrace();
            ViewManager.showMessageDialog("service.ManhuaGui.parsePage.io_exception",
                    viewManager.getView(), e.toString());
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().endsWith("manhuagui.com");
    }

    public String getInfo() {
        return "ManhuaGui: manhuagui.com";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
