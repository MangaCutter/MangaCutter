package net.macu.browser.proxy.server;

import net.macu.browser.proxy.CapturedImageProcessor;
import net.macu.browser.proxy.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPProxy extends Thread {
    private final int port;
    private ServerSocket ss;
    private final CapturedImageProcessor capturedImages;

    public HTTPProxy(int port, CapturedImageProcessor capturedImages) {
        this.capturedImages = capturedImages;
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
                Handler handler = new Handler(s.getInputStream(), s.getOutputStream(), false, capturedImages);
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
