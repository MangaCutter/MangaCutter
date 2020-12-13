package net.macu.browser.image_proxy.proxy;

import java.io.IOException;
import java.net.ServerSocket;

public class HTTPProxy extends Thread {
    private final int port;
    private ServerSocket ss;

    public HTTPProxy(int port) {
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
                Handler handler = new Handler(ss.accept(), false);
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
