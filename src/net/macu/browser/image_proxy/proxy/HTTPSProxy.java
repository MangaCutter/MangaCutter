package net.macu.browser.image_proxy.proxy;

import net.macu.browser.image_proxy.CapturedImageMap;
import net.macu.util.UnblockableBufferedReader;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HTTPSProxy {
    private static final int MAX_CONNECTIONS = 30;
    private final int port;
    private final CapturedImageMap capturedImages;
    private final ArrayList<Socket> sockets = new ArrayList<>();
    private SSLServerSocket sslListener;

    public HTTPSProxy(int port, CapturedImageMap capturedImages) {
        this.port = port;
        this.capturedImages = capturedImages;
    }

    public void start() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[]{ExtendableX509KeyManager.getInstance()}, null, null);
            SSLServerSocketFactory factory = ctx.getServerSocketFactory();
            ServerSocket listener = factory.createServerSocket(port);
            System.out.println("HTTPS Proxy started on port " + port);
            sslListener = (SSLServerSocket) listener;
            sslListener.setNeedClientAuth(false);
            SSLParameters sslParams = ctx.getSupportedSSLParameters();
            SSLEngine engine = ctx.createSSLEngine();
            sslParams.setNeedClientAuth(false);
            sslParams.setCipherSuites(engine.getEnabledCipherSuites());
            sslParams.setProtocols(engine.getEnabledProtocols());
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
            sslListener.setSSLParameters(sslParams);
           /* while (true) {
                for (int i = 0; i < sockets.size(); i++) {
                    if (sockets.get(i).isClosed()) {
                        sockets.remove(i);
                        i--;
                    }
                }
                if (sockets.size() < MAX_CONNECTIONS) {
                    System.out.println(sockets.size());
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
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void pipe(UnblockableBufferedReader in, OutputStream out, String targetHost) throws IOException {
        try {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            ExtendableX509KeyManager.addDomain(targetHost);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return;
        }
        Socket toProxy = null;
        Socket afterProxy = null;
        try {
            toProxy = new Socket("127.0.0.1", port);
            afterProxy = sslListener.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HTTPSPipe.addPipe(new HTTPSPipe.Pipe(in, out, toProxy.getInputStream(), toProxy.getOutputStream(), afterProxy));
        new Handler(afterProxy.getInputStream(), afterProxy.getOutputStream(), true, capturedImages, this).start();
//        new HTTPSHandler(in, out, toProxy, afterProxy, capturedImages, this).start();
    }
}