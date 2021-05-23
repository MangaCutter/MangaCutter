package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.core.IOManager;
import net.macu.downloader.SimpleDownloader;
import net.macu.settings.L;
import net.macu.util.JSEngine;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.EvaluatorException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AcQq implements Service {
    private boolean cancel = false;

    @Override
    public BufferedImage[] parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.AcQq.parsePage.progress"));
        try {
            String jsonData = "";
            while (true) {
                if (cancel) return null;
                String html = IOManager.sendRequest(uri);
                String[] parts = html.split("<script>");
                if (parts.length != 4) {
                    ViewManager.showMessageDialog("service.AcQq.parsePage.unknown_format", viewManager.getView());
                    return null;
                }
                String keyEval = parts[2].substring(0, parts[2].indexOf("</script>")).trim();
                keyEval = keyEval.replaceAll("document\\.children", "true");
                if (keyEval.contains("document")) {
                    continue;
                }
                String encryptedData = parts[3].substring(parts[3].indexOf('\'') + 1, parts[3].lastIndexOf('\''));
                String code = "window = {\"nonce\":\"\",\"DATA\":\"\"};\n" +
                        keyEval + "\n" +
                        "window[\"DATA\"] = '" + encryptedData + " ';\n" +
                        "function Base() {\n" +
                        "    _keyStr = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\";\n" +
                        "    this.decode = function(c) {\n" +
                        "        var a = \"\",\n" +
                        "            b, d, h, f, g, e = 0;\n" +
                        "        for (c = c.replace(/[^A-Za-z0-9\\+\\/\\=]/g, \"\"); e < c.length;) b = _keyStr.indexOf(c.charAt(e++)), d = _keyStr.indexOf(c.charAt(e++)), f = _keyStr.indexOf(c.charAt(e++)), g = _keyStr.indexOf(c.charAt(e++)), b = b << 2 | d >> 4, d = (d & 15) << 4 | f >> 2, h = (f & 3) << 6 | g, a += String.fromCharCode(b), 64 != f && (a += String.fromCharCode(d)), 64 != g && (a += String.fromCharCode(h));\n" +
                        "        return a = _utf8_decode(a)\n" +
                        "    };\n" +
                        "    _utf8_decode = function(c) {\n" +
                        "        for (var a = \"\", b = 0, d = c1 = c2 = 0; b < c.length;) d = c.charCodeAt(b), 128 > d ? (a += String.fromCharCode(d), b++) : 191 < d && 224 > d ? (c2 = c.charCodeAt(b + 1), a += String.fromCharCode((d & 31) << 6 | c2 & 63), b += 2) : (c2 = c.charCodeAt(b + 1), c3 = c.charCodeAt(b + 2), a += String.fromCharCode((d & 15) << 12 | (c2 & 63) << 6 | c3 & 63), b += 3);\n" +
                        "        return a\n" +
                        "    }\n" +
                        "}\n" +
                        "var B = new Base(),\n" +
                        "    T = window['DA' + 'TA'].split(''),\n" +
                        "    N = window['n' + 'onc' + 'e'],\n" +
                        "    len, locate, str;\n" +
                        "N = N.match(/\\d+[a-zA-Z]+/g);\n" +
                        "len = N.length;\n" +
                        "while (len--) {\n" +
                        "    locate = parseInt(N[len]) & 255;\n" +
                        "    str = N[len].replace(/\\d+/g, '');\n" +
                        "    T.splice(locate, str.length)\n" +
                        "}\n" +
                        "T = T.join('');\n" +
                        "B.decode(T)";
                try {
                    jsonData = (String) JSEngine.evaluate(code);
                    break;
                } catch (EvaluatorException ignored) {
                }
            }
            try {
                ArrayList json = (ArrayList) new JSONParser().parse(jsonData.substring(jsonData.indexOf("["), jsonData.indexOf("]") + 1));
                return SimpleDownloader.downloadFragments((List<String>) json.stream().map((o) -> ((JSONObject) o).get("url")).collect(Collectors.toList()), viewManager);
            } catch (ParseException e) {
                e.printStackTrace();
                ViewManager.showMessageDialog("service.AcQq.parsePage.parse_exception",
                        viewManager.getView(), e.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            ViewManager.showMessageDialog("service.AcQq.parsePage.io_exception",
                    viewManager.getView(), e.toString());
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("ac.qq.com");
    }

    public String getInfo() {
        return "Ac.Qq: ac.qq.com";
    }

    @Override
    public void cancel() {
        cancel = true;
        SimpleDownloader.cancel();
    }
}
