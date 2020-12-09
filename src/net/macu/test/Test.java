package net.macu.test;

import net.macu.browser.image_proxy.proxy.HTTPSProxy;
import net.macu.browser.image_proxy.proxy.MainGatewayProxy;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class Test {

    public static void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Info");
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
        /*ServerSocket ss = null;
        try {
            ss = new ServerSocket(50001);
            BufferedReader bf = new BufferedReader(new InputStreamReader(ss.accept().getInputStream()));
            String s;
            while ((s = bf.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServerSocket finalSs = ss;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                wss.stop();
                finalSs.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }));*/
        MainGatewayProxy server = new MainGatewayProxy();
        server.start();
        HTTPSProxy httpsProxy = new HTTPSProxy();
        httpsProxy.start();
        try {
            server.join();
            httpsProxy.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*KeyStore keyStore = KeyStore.getInstance("pkcs12");
        InputStream kstore = new FileInputStream("/home/user/ca/mangafreaks.p12");
        keyStore.load(kstore, new char[]{'a', 'b', 'c', 'd', 'e', 'f'});
//        kstore = new FileInputStream("/home/user/ca/keystore.p12");
//        keyStore.load(kstore, new char[]{'a', 'b', 'c', 'd', 'e', 'f'});
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, new char[]{'a', 'b', 'c', 'd', 'e', 'f'});
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory factory = ctx.getServerSocketFactory();
        try (ServerSocket listener = factory.createServerSocket(50002)) {
            SSLServerSocket sslListener = (SSLServerSocket) listener;
            sslListener.setNeedClientAuth(false);
            SSLParameters sslParams = ctx.getSupportedSSLParameters();
            SSLEngine engine = ctx.createSSLEngine();
            sslParams.setNeedClientAuth(false);
            sslParams.setCipherSuites(engine.getEnabledCipherSuites());
            sslParams.setProtocols(engine.getEnabledProtocols());
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
            sslListener.setSSLParameters(sslParams);
            // NIO to be implemented
            while (true) {
                try (SSLSocket socket = (SSLSocket) sslListener.accept()) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    BufferedReader br = new BufferedReader(socket.getInputStream());
                    String buffer;
                    while ((buffer = br.readLine(false)) != null) System.out.println(buffer);
                    out.println("Hello World!");
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }*/
        /*new PrintStream(new FileOutputStream("test")).print("abcdef\nerror\r\n_)o\n");
        BufferedReader br = new BufferedReader(new FileReader("test"));
        char[] buff = new char[50];
        int size = br.read(buff);
        for (char c : buff) {
            System.out.print(Integer.toHexString(c)+" ");
        }*/

//        Plugin.initServer();
//        Plugin.waitUntilConnected(5);
//        Plugin.openUrlInDefaultBrowser("https://images.mangafreak.net/mangas/uzaki_chan_wa_asobitai/uzaki_chan_wa_asobitai_3/uzaki_chan_wa_asobitai_3_8.jpg");

    }
}
