package net.macu.service;

import net.macu.core.Main;

public class ServiceManager {
    public static Service getService(String uri) {
        if (uri == null) {
            throw new NullPointerException();
        }
        for (Service s : Main.getServices()) {
            try {
                //the second part of check is just for case of developers stupidity
                if (s.accept(uri) && (s.supportsNativeDownloading() || s.supportsBrowserDownloading())) {
                    return s;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static String getSupportedServicesList() {
        StringBuilder sb = new StringBuilder();
        Main.getServices().forEach(service -> sb.append(service.getInfo()).append("\n"));
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
