package net.macu.browser.image_proxy.proxy;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.security.KeyStore;

public class HTTPSProxy extends Thread {

    @Override
    public void run() {
        try {
            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            InputStream kstore = new FileInputStream("/home/user/ca/mangafreaks.p12");
            keyStore.load(kstore, new char[]{'a', 'b', 'c', 'd', 'e', 'f'});
//        kstore = new FileInputStream("/home/user/ca/keystore.p12");
//        keyStore.load(kstore, new char[]{'a', 'b', 'c', 'd', 'e', 'f'});
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, new char[]{'a', 'b', 'c', 'd', 'e', 'f'});
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory factory = ctx.getServerSocketFactory();
            try (ServerSocket listener = factory.createServerSocket(50002)) {
                SSLServerSocket sslListener;
                sslListener = (SSLServerSocket) listener;
                sslListener.setNeedClientAuth(false);
                SSLParameters sslParams = ctx.getSupportedSSLParameters();
                SSLEngine engine = ctx.createSSLEngine();
                sslParams.setNeedClientAuth(false);
                sslParams.setCipherSuites(engine.getEnabledCipherSuites());
                sslParams.setProtocols(engine.getEnabledProtocols());
                sslParams.setEndpointIdentificationAlgorithm("HTTPS");
                sslListener.setSSLParameters(sslParams);
                while (true) {
                    try {
                        SSLSocket socket = (SSLSocket) sslListener.accept();
                        new Handler(socket, true).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
