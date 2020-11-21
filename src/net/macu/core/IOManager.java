package net.macu.core;

import net.macu.UI.ViewManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class IOManager {
    private static CloseableHttpClient client = null;

    public static String sendRequest(String uri) throws IOException {
        StringBuilder sb = new StringBuilder();
        CloseableHttpResponse response = client.execute(new HttpGet(uri));
        BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String s;
        while ((s = bf.readLine()) != null) {
            sb.append(s).append('\n');
        }
        bf.close();
        response.close();
        return sb.toString();
    }

    public static InputStream rawSendRequest(String uri) throws IOException {
        return client.execute(new HttpGet(uri)).getEntity().getContent();
    }

    public static BufferedImage downloadImage(String uri) throws IOException {
        return ImageIO.read(client.execute(new HttpGet(uri)).getEntity().getContent());
    }

    public static void initClient() {
        if (client == null) {
            client = HttpClients.custom()
                    .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
                    .setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0")
                    .setConnectionTimeToLive(20, TimeUnit.SECONDS)
                    .build();
        }
    }

    public static void checkUpdates() {
        CloseableHttpClient cs = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(false).build()).build();
        try {
            String location = cs.execute(new HttpGet("https://github.com/MasterLogick/MangaCutter/releases/latest"))
                    .getFirstHeader("location").getValue();
            String tag = location.substring(location.lastIndexOf("/") + 1);
            if (!tag.equals(Main.getVersion())) {
                ViewManager.showMessageDialog("Доступна новая версия MaCu<br><a href=\"" + location + "\">" + location + "</a>");
            }
        } catch (IOException e) {
            ViewManager.showMessageDialog("Не удалось получить информацию о новых версиях программы\n" + e.toString());
            e.printStackTrace();
        }
        try {
            cs.close();
        } catch (IOException e) {
            ViewManager.showMessageDialog("Не удалось закрыть клиент.\n(Как Вы вообще сумели получить эту ошибку?)\n" + e.toString());
            e.printStackTrace();
        }
    }
}
