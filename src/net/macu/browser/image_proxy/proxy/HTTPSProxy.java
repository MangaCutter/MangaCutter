package net.macu.browser.image_proxy.proxy;

import javax.net.ssl.*;
import java.net.ServerSocket;

public class HTTPSProxy extends Thread {
    private final int port;

    public HTTPSProxy(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[]{ExtendableX509KeyManager.getInstance()}, null, null);
            SSLServerSocketFactory factory = ctx.getServerSocketFactory();
            ServerSocket listener = factory.createServerSocket(port);
            System.out.println("HTTPS Proxy started on port " + port);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
