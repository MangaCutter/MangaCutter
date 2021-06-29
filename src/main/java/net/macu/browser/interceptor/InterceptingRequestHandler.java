package net.macu.browser.interceptor;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;

public class InterceptingRequestHandler extends CefRequestHandlerAdapter {
    private final InterceptingResourceRequestHandler handler = new InterceptingResourceRequestHandler();

    @Override
    public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request,
                                                               boolean isNavigation, boolean isDownload,
                                                               String requestInitiator,
                                                               BoolRef disableDefaultHandling) {
        disableDefaultHandling.set(true);
        return handler;
    }
}
