package net.ddns.logick;

import java.net.URI;
import java.util.List;

public interface ServiceParser {

    /**
     * Parses manga page and return resolved URIs to images in logical order
     *
     * @param path link to manga page
     * @return {@link java.util.List} of {@link java.net.URI} in logical order
     */
    List<URI> parsePage(String path);

    /**
     * Stops parsing of the page
     */
    void stop();
}
