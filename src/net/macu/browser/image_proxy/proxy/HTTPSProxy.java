package net.macu.browser.image_proxy.proxy;

import net.macu.browser.image_proxy.CapturedImageMap;

import javax.net.ssl.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HTTPSProxy extends Thread {
    private static final int MAX_CONNECTIONS = 30;
    private final int port;
    private final CapturedImageMap capturedImages;
    private final ArrayList<Socket> sockets = new ArrayList<>();

    public HTTPSProxy(int port, CapturedImageMap capturedImages) {
        this.port = port;
        this.capturedImages = capturedImages;
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
                for (int i = 0; i < sockets.size(); i++) {
                    if (sockets.get(i).isClosed()) {
                        sockets.remove(i);
                        i--;
                    }
                }
                if (sockets.size() < MAX_CONNECTIONS) {
                    try {
                        SSLSocket socket = (SSLSocket) sslListener.accept();
                        sockets.add(socket);
                        new Handler(socket.getInputStream(), socket.getOutputStream(), true, capturedImages).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Thread.yield();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}