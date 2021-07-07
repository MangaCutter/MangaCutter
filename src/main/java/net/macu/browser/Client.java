package net.macu.browser;

import net.macu.browser.handler.LoadHandler;
import net.macu.browser.handler.MessageRouterHandler;
import net.macu.browser.interceptor.InterceptingRequestHandler;
import net.macu.browser.interceptor.Interceptor;
import net.macu.settings.Settings;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Client {
    private static Client client = null;
    private static CefClient cefClient = null;
    private final HashMap<UUID, CompletableFuture<String[]>> scriptResults = new HashMap<>();
    private final HashMap<CefBrowser, CompletableFuture<Void>> scripts =
            new HashMap<>();

    private Client() {
    }

    public static void initialise() {
        if (!JCefLoader.isLoaded()) {
            if (isLoadable()) {
                CefSettings settings = new CefSettings();
                settings.windowless_rendering_enabled = true;
                settings.user_agent = Settings.IOManager_UserAgent.getValue();
                settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_INFO;
                CefApp app = JCefLoader.loadCef(
                        new File(Settings.Client_InstallationFolder.getValue()),
                        settings);
                //lol it just don't work without invocation of CefApp::getVersion() method
                System.out.println(app.getVersion());
                cefClient = app.createClient();
                client = new Client();
                //todo add handlers
                cefClient.addLoadHandler(new LoadHandler(client));
                cefClient.addRequestHandler(new InterceptingRequestHandler());
                cefClient.addMessageRouter(CefMessageRouter.create(
                        new CefMessageRouter.CefMessageRouterConfig(
                                MessageRouterHandler.QUERY_FUNCTION_NAME,
                                MessageRouterHandler.CANCEL_FUNCTION_NAME),
                        new MessageRouterHandler(client)
                ));
            }
        }
    }

    public static Client getInstance() {
        if (client == null) initialise();
        return client;
    }

    public static boolean isLoadable() {
        return !Settings.Client_InstallationFolder.getValue().isEmpty();
    }

    public CefBrowser createBrowser(String url, boolean isOffscreen) {
        CefBrowser cefBrowser = cefClient.createBrowser(url, isOffscreen, false);
        if (isOffscreen) cefBrowser.createImmediately();
        Component browserUI = cefBrowser.getUIComponent();
        MouseWheelListener[] listeners = browserUI.getMouseWheelListeners();
        for (MouseWheelListener listener : listeners) {
            browserUI.removeMouseWheelListener(listener);
        }
        browserUI.addMouseWheelListener(e -> {
            for (MouseWheelListener listener : listeners) {
                listener.mouseWheelMoved(new MouseWheelEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(),
                        e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(),
                        e.getWheelRotation() * Settings.ViewManager_MasterScrollSpeed.getValue() *
                                (Settings.ViewManager_MasterScrollInversion.getValue() ? 1 : -1)));
            }
        });
        return cefBrowser;
    }

    public CompletableFuture<String[]> getResultCompletableFuture(UUID uuid) {
        synchronized (scriptResults) {
            if (!scriptResults.containsKey(uuid)) {
                scriptResults.put(uuid, new CompletableFuture<>());
            }
            return scriptResults.get(uuid);
        }
    }

    public CompletableFuture<BufferedImage[]> getResult(UUID uuid) {
        CompletableFuture<String[]> scriptResult = getResultCompletableFuture(uuid);
        return scriptResult.thenApply(paths -> {
            System.out.println("got paths " + paths.length);
            CompletableFuture<BufferedImage>[] imageFutures = Arrays.stream(paths).map(Interceptor::getImageFuture).toArray(CompletableFuture[]::new);
            try {
                CompletableFuture.allOf(imageFutures).get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
            return Arrays.stream(imageFutures).map(imageFuture -> {
                try {
                    return imageFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    return null;
                }
            }).toArray(BufferedImage[]::new);
        });
    }

    public void removeResultData(UUID uuid) {
        scriptResults.remove(uuid);
    }

    public CompletableFuture<Void> getScriptCompletableFuture(CefBrowser browser) {
        synchronized (scripts) {
            if (!scripts.containsKey(browser)) {
                scripts.put(browser, new CompletableFuture<>());
            }
            return scripts.get(browser);
        }
    }

    public void injectOnLoad(CefBrowser browser, String script) {
        getScriptCompletableFuture(browser).thenAccept((v) -> {
            browser.executeJavaScript(script, "", 0);
            System.out.println("script executed");
        });
    }

    public void dispose() {
        cefClient.dispose();
    }
}
