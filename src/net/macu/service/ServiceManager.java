package net.macu.service;

import net.macu.core.Main;

public class ServiceManager {
    public static Service getService(String uri) {
        if (uri.isEmpty()) return null;
        for (Service s : Main.getServices()) {
            if (s.accept(uri)) {
                return s;
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
