package net.macu.test;

import net.macu.browser.image_proxy.proxy.CertUtils;
import net.macu.browser.image_proxy.proxy.HTTPSPipe;
import net.macu.browser.image_proxy.proxy.HTTPSProxy;
import net.macu.browser.image_proxy.proxy.MainGatewayProxy;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.security.Security;

public class Test {

    public static void main(String[] args) throws Exception {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Info");
        Security.addProvider(new BouncyCastleProvider());
//        System.setProperty("javax.net.debug", "ssl");
        /*FileWriter fw = new FileWriter("test1");
        fw.write("ааа_rrr\r\neee\ntttt\u0080");
        fw.close();
//        FileInputStream fis = new FileInputStream("test1");
        InputStream testIS = new InputStream() {
            int i = 0;

            @Override
            public int read() {
                System.out.println(++i);
                return i;
            }
        };

        InputStreamReader isr = new InputStreamReader(testIS);
        char[] c = new char[2];
        System.out.println(isr.read(c));
        System.out.println(c);
        byte[] b = new byte[2];
        System.out.println(testIS.read(b));
        System.out.println(Arrays.toString(b));*/
//        BufferedReader bf = new BufferedReader(new FileReader("test1"));
//        char[] c = bf.re
        /*IOManager.initClient();
        WebSocketServer wss = new WebSocketServer(new InetSocketAddress(50000)) {
            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                System.out.println("client connected");
            }

            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                System.out.println("closed");
            }

            @Override
            public void onMessage(WebSocket webSocket, String s) {
                System.out.println(s);
            }

            @Override
            public void onError(WebSocket webSocket, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("started");
            }
        };
        wss.start();*/
//        System.out.println(CertUtils.generate("test.domain", CertUtils.readPKCS12File(new FileInputStream("/home/user/ca/ca.p12"))).toString());
        System.out.println(CertUtils.readPKCS12File(new FileInputStream("/home/user/ca/ca.p12")).getCertificate().toString());
        CertUtils.generateRootCA();
        MainGatewayProxy server = new MainGatewayProxy();
        server.start();
        HTTPSProxy httpsProxy = new HTTPSProxy();
        httpsProxy.start();
        HTTPSPipe.startHandler();
        try {
            server.join();
            httpsProxy.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
