package net.macu.browser;

import net.macu.UI.IconManager;
import net.macu.browser.handler.MessageRouterHandler;
import net.macu.settings.History;
import net.macu.settings.L;
import net.macu.settings.Settings;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OffscreenBrowser {

    public static BufferedImage[] executeScript(String url, String script) {
        if (JCefLoader.isLoaded() && Client.getInstance() != null) {
            Client client = Client.getInstance();
            CefBrowser browser = client.createBrowser(url, true);
            JFrame frame = History.createJFrameFromHistory("browser.OffscreenBrowser.frame_title", 100, 100);
            frame.setTitle(L.get("browser.OffscreenBrowser.frame_title"));
            frame.setIconImage(IconManager.getBrandImage());
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.getContentPane().add(browser.getUIComponent());
            frame.setVisible(true);
            UUID uuid = UUID.randomUUID();
            String jsIS = String.format("macuIS = %s;\n" +
                            "result = async () => macuIS();\n" +
                            "result().then(res => window.%s({request: JSON.stringify({uuid: \"%s\", paths: res}), persistent: false, onSuccess: function(response) {}, onFailure: function(error_code, error_message) {} }));\n",
                    script, MessageRouterHandler.QUERY_FUNCTION_NAME, uuid);
            System.out.println(jsIS);
            client.injectOnLoad(browser, jsIS);
            BufferedImage[] result = null;
            try {
                result = client.getResult(uuid).get(Settings.OffscreenBrowser_Timeout.getValue(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            frame.dispose();
            browser.close(true);
            client.removeResultData(uuid);
            //todo cleanup
//            client.cleanData(browser.getIdentifier());
            return result;
        }
        throw new RuntimeException("Cef is not loaded");
    }
}
