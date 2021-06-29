package net.macu.browser.handler;

import net.macu.browser.Client;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;

public class LoadHandler implements CefLoadHandler {
    private final Client client;

    public LoadHandler(Client client) {
        this.client = client;
    }

    @Override
    public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
        if (!isLoading) {
            System.out.println("browser " + browser.getIdentifier() + " " + isLoading + " " + canGoBack + " " + canGoForward);
            client.getScriptCompletableFuture(browser).complete(null);
        }
    }

    @Override
    public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {
    }

    @Override
    public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
    }

    @Override
    public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
    }
}
