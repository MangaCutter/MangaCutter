package net.macu.browser.image_proxy.proxy;

import net.macu.util.UnblockableBufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class HTTPSPipe extends Thread {
    private static final Vector<Pipe> pipes = new Vector<>();
    private static final HTTPSPipe handler = new HTTPSPipe();

    private HTTPSPipe() {
        setDaemon(true);
    }

    public static void startHandler() {
        if (!handler.isAlive()) handler.start();
    }

    public static void pipe(UnblockableBufferedReader in, OutputStream out, Socket client, String targetHost) {
        try {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            ExtendableX509KeyManager.addKeyManager(targetHost);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return;
        }
        Socket s = null;
        try {
            s = new Socket("127.0.0.1", 50002);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (s != null) {
            InputStream dIn = null;
            OutputStream dOut = null;
            try {
                dIn = s.getInputStream();
                dOut = s.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pipes.add(new Pipe(dIn, out));
            pipes.add(new Pipe(in, dOut));
        } else {
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (true) {
            if (pipes.size() == 0) Thread.yield();
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                try {
                    if (pipe.in.available() > 0) {
                        int received = pipe.in.read(buffer);
                        if (received < 0) {
                            pipes.remove(i);
                            i--;
                            continue;
                        }
                        pipe.out.write(buffer, 0, received);
                    }
                } catch (IOException e) {
                    pipes.remove(i);
                    i--;
                }
            }
        }
    }

    private static class Pipe {
        InputStream in;
        OutputStream out;

        private Pipe(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }
    }
}
