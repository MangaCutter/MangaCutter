package net.macu.service;

import net.macu.UI.ViewManager;

import java.awt.image.BufferedImage;

public interface Service {
    /**
     * @return True if this Service can parse page from uri
     */
    boolean accept(String uri);

    /**
     * @return True if this Service supports native downloading
     */
    boolean supportsNativeDownloading();

    /**
     * Natively parses page and returns array of {@link BufferedImage} in logical order
     *
     * @param uri         Link to manga chapter of this Service
     * @param viewManager ViewManager instance to show interact with user
     */
    BufferedImage[] parsePage(String uri, ViewManager viewManager);

    /**
     * @return True if this service supports browser downloading
     */
    boolean supportsBrowserDownloading();

    /**
     * JS script for obtaining resolved URIs to images in logical order.
     * Output on the same pages must be equal to {@link Service#parsePage(String, ViewManager)}
     *
     * @return Corresponding JS script
     */
    String getBrowserInjectingScript();

    /**
     * @return Short info about Service parser
     */
    String getInfo();

    /**
     * Stops current parsing job
     */
    void cancel();
}
