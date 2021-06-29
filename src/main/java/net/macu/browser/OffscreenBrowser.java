package net.macu.browser;

import net.macu.browser.handler.MessageRouterHandler;
import net.macu.settings.Settings;
import org.cef.browser.CefBrowser;

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

            UUID uuid = UUID.randomUUID();
            String jsIS = String.format("macuIS = %s;" +
                            "window.%s({request: JSON.stringify({uuid: \"%s\", paths: macuIS()}), persistent: false, onSuccess: function(response) {}, onFailure: function(error_code, error_message) {} });",
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
            browser.close(true);
            client.removeResultData(uuid);
            client.cleanData(browser.getIdentifier());
            return result;
        }
        throw new RuntimeException("Cef is not loaded");
    }
}
