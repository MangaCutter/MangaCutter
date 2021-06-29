package net.macu.browser.interceptor;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.network.CefRequest;

public class InterceptingResourceRequestHandler extends CefResourceRequestHandlerAdapter {
    @Override
    public CefResourceHandler getResourceHandler(CefBrowser browser, CefFrame frame, CefRequest request) {
        return new InterceptingResourceHandler(browser == null ? Integer.MIN_VALUE : browser.getIdentifier());
    }
}
