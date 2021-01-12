package net.macu.core;

import net.macu.UI.ViewManager;
import net.macu.settings.L;
import net.macu.settings.Parameter;
import net.macu.settings.Parameters;
import net.macu.settings.Parametrized;
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

public class IOManager implements Parametrized {
    private static final Parameter USER_AGENT = new Parameter(Parameter.Type.STRING_TYPE, "core.IOManager.user_agent");
    private static CloseableHttpClient client = null;

    public static String sendRequest(String uri) throws IOException {
        StringBuilder sb = new StringBuilder();
        CloseableHttpResponse response = client.execute(new HttpGet(uri));
        BufferedReader bf = new BufferedReader(new InputStreamReader(sendRawRequest(uri)));
        String s;
        while ((s = bf.readLine()) != null) {
            sb.append(s).append('\n');
        }
        bf.close();
        response.close();
        return sb.toString();
    }

    public static InputStream sendRawRequest(String uri) throws IOException {
        return client.execute(new HttpGet(uri)).getEntity().getContent();
    }

    public static BufferedImage downloadImage(String uri) throws IOException {
        return ImageIO.read(client.execute(new HttpGet(uri)).getEntity().getContent());
    }

    public static void initClient() {
        if (client == null) {
            client = HttpClients.custom()
                    .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
                    .setUserAgent(USER_AGENT.getString())
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
                ViewManager.showMessageDialog(
                        L.get("core.IOManager.checkUpdates.new_ver", location, location), null);
            }
        } catch (IOException e) {
            ViewManager.showMessageDialog(
                    L.get("core.IOManager.checkUpdates.execute_exception", e.toString()), null);
            e.printStackTrace();
        }
        try {
            cs.close();
        } catch (IOException e) {
            ViewManager.showMessageDialog(
                    L.get("core.IOManager.checkUpdates.close_client_exception", e.toString()), null);
            e.printStackTrace();
        }
    }

    public static Parameters getParameters() {
        return new Parameters("core.IOManager", USER_AGENT);
    }
}
