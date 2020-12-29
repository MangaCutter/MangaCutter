package net.macu.browser.image_proxy.proxy;

import net.macu.browser.image_proxy.CapturedImageMap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPProxy extends Thread {
    private final int port;
    private ServerSocket ss;
    private final CapturedImageMap capturedImages;
    private HTTPSProxy httpsProxy;

    public HTTPProxy(int port, CapturedImageMap capturedImages, HTTPSProxy httpsProxy) {
        this.capturedImages = capturedImages;
        this.httpsProxy = httpsProxy;
        setDaemon(true);
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("HTTP Proxy has been started on port " + port);
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (true) {
                Socket s = ss.accept();
                Handler handler = new Handler(s.getInputStream(), s.getOutputStream(), false, capturedImages, httpsProxy);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isStarted() {
        return isAlive() && ss.isBound();
    }
}
