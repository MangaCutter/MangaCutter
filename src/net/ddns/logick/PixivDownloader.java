package net.ddns.logick;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class PixivDownloader {
    public static void main(String[] args) {
        CloseableHttpClient client = HttpClients.custom()
                .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
                .setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0")
                .setConnectionTimeToLive(20, TimeUnit.SECONDS)
                .build();
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String pref = "\"regular\":\"";
        int dir = 4;
        while (true) {
            String s = null;
            try {
                s = readData(client.execute(new HttpGet(bf.readLine())).getEntity().getContent());
            } catch (IOException e) {
                e.printStackTrace();//todo make better capturing
            }
            s = s.substring(s.indexOf(pref) + pref.length());
            s = s.substring(0, s.indexOf("\""));
            String first = s.substring(0, 65);
            String last = s.substring(66);
            internal:for (int i = 0; true; i++) {
                new File(dir + "").mkdir();
                try {
                    BufferedImage bi = ImageIO.read(client.execute(new HttpGet(first + i + last)).getEntity().getContent());
                    ImageIO.write(bi, "jpg", new File(dir + File.separator + (i+1) + ".jpg"));
                } catch (IOException e) {
                    break internal;
                }
            }
        }
    }

    public static String readData(InputStream stream) throws IOException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(stream));
        String s = null;
        StringBuffer sb = new StringBuffer();
        while ((s = bf.readLine()) != null) {
            sb.append(s).append('\n');
        }
        return sb.toString();
    }
}
