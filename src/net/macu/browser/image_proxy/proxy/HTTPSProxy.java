package net.macu.browser.image_proxy.proxy;

import org.bouncycastle.util.io.Streams;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.ServerSocket;

public class HTTPSProxy extends Thread {

    @Override
    public void run() {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(CertificateAuthority.readPKCS12File(Streams.readAll(new FileInputStream("/home/user/ca/ca.p12"))).generateSubKeyStore("*.mangafreak.net"), null);
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
