package net.ddns.masterlogick.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class IOManager {
    private static CloseableHttpClient client = null;

    public static String sendRequest(String uri) throws Exception {
        checkClient();
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

    public static InputStream rawSendRequest(String uri) throws Exception {
        checkClient();
        return client.execute(new HttpGet(uri)).getEntity().getContent();
    }

    public static BufferedImage downloadImage(String uri) throws Exception {
        checkClient();
        return ImageIO.read(client.execute(new HttpGet(uri)).getEntity().getContent());
    }

    private static void checkClient() throws Exception {
        if (client == null) {
            throw new Exception("Client must be initialised before network requests!");
        }
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
}
