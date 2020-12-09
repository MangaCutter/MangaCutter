package net.macu.browser.image_proxy.proxy;

import java.io.IOException;
import java.net.ServerSocket;

public class MainGatewayProxy extends Thread {

//    private ArrayList<Handler> handlersPool;

    public MainGatewayProxy() {
        setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("Proxy has been started");
        try (ServerSocket ss = new ServerSocket(50001)) {
            while (true) {
                Handler handler = new Handler(ss.accept(), false);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
