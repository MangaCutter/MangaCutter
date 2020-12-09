package net.macu.service;

import java.util.List;

public interface Service {
    /**
     * Parses manga page and return resolved URIs to images in logical order
     *
     * @param uri link to manga chapter of this service
     */
    List<String> parsePage(String uri);

    /**
     * Whether the given uri is belongs to this service
     */
    boolean accept(String uri);

    /**
     * Returns service name and address separated with colon
     *
     * @return short oneline info about service parser
     */
    String getInfo();

    /**
     * Stops current parsing job
     */
    void cancel();
}
