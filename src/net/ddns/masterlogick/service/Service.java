package net.ddns.masterlogick.service;

import java.util.List;

public interface Service {

    /**
     * Parses manga page and return resolved URIs to images in logical order
     * @param uri
     */
    List<String> parsePage(String uri);

    /**
     * Whether the given uri is belongs to this service
     */
    static boolean accept(String uri) {
        return false;
    }

    /**
     * Stops current parsing job
     */
    void cancel();
}
