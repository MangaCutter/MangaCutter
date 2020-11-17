package net.ddns.masterlogick.service;

import java.util.List;

public class Service {
    public Service() {
    }

    /**
     * Parses manga page and return resolved URIs to images in logical order
     *
     * @param uri link to manga chapter of this service
     */
    public List<String> parsePage(String uri) {
        return null;
    }

    /**
     * Whether the given uri is belongs to this service
     */
    public static boolean accept(String uri) {
        return false;
    }

    /**
     * Returns service name and address separated with colon
     *
     * @return short oneline info about service parser
     */
    public static String getInfo() {
        return "Unimplemented service: example.com";
    }

    /**
     * Stops current parsing job
     */
    public void cancel() {
    }
}
