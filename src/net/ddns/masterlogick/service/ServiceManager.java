package net.ddns.masterlogick.service;

public class ServiceManager {
    public static Service getService(String uri) {
        if (uri.isEmpty()) return null;
        if (Naver.accept(uri)) {
            return new Naver();
        }
        return null;
    }

    public static String getSupportedServicesList() {
        return Naver.getInfo();
    }
}
