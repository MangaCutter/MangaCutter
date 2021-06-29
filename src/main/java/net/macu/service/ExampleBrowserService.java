package net.macu.service;

import net.macu.UI.ViewManager;

import java.awt.image.BufferedImage;

public class ExampleBrowserService implements Service {
    @Override
    public boolean accept(String uri) {
        return uri.equals("aaabbb");
    }

    @Override
    public boolean supportsNativeDownloading() {
        return false;
    }

    @Override
    public BufferedImage[] parsePage(String uri, ViewManager viewManager) {
        return new BufferedImage[0];
    }

    @Override
    public boolean supportsBrowserDownloading() {
        return true;
    }

    @Override
    public String getBrowserInjectingScript() {
        return "() => []";
    }

    @Override
    public String getInfo() {
        return "";
    }

    @Override
    public void cancel() {

    }
}
