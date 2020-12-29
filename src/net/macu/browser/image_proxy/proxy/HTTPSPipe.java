package net.macu.browser.image_proxy.proxy;

import net.macu.browser.image_proxy.CapturedImageMap;
import net.macu.util.UnblockableBufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class HTTPSPipe extends Thread {
    private static final Vector<Pipe> pipes = new Vector<>();
    private static final HTTPSPipe handler = new HTTPSPipe();

    private HTTPSPipe() {
        setDaemon(true);
        setName("HTTPSPipe");
    }

    public static void startHandler() {
        if (!handler.isAlive()) handler.start();
    }

    public static void addPipe(Pipe pipe) {
        pipes.add(pipe);
    }

   /* public static synchronized void pipe(UnblockableBufferedReader in, OutputStream out, String targetHost, CapturedImageMap capturedImages) {
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
        Socket s = null;
        try {
            s = new Socket("127.0.0.1", 50006);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (s != null) {
            InputStream inS = null;
            OutputStream outS = null;
            try {
                inS = s.getInputStream();
                outS = s.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pipes.add(new Pipe(in, out, inS, outS, s));
        } else {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public void run() {
        byte[] buffer = new byte[1024 * 16];
        int readS;
        int readC;
        while (true) {
            long minWait = Long.MAX_VALUE;
            long maxWait = 0;
            if (pipes.size() == 0) Thread.yield();
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                try {
                    long from = System.currentTimeMillis();
                    do {
                        if (pipe.inS.available() > 0) {
                            while ((readS = pipe.inS.read(buffer)) == buffer.length) {
                                pipe.outC.write(buffer, 0, readS);
                            }
                            pipe.outC.write(buffer, 0, readS);
                        } else {
                            readS = 0;
                        }
                        if (pipe.inC.available() > 0) {
                            while ((readC = pipe.inC.read(buffer)) == buffer.length) {
                                pipe.outS.write(buffer, 0, readC);
                            }
                            pipe.outS.write(buffer, 0, readC);
                        } else {
                            readC = 0;
                        }
                    } while (readS + readC != 0);
                    if (pipe.s.isClosed()) {
                        pipes.remove(i);
                        pipe.close();
                        i--;
                    }
                    long to = System.currentTimeMillis();
                    minWait = Math.min(minWait, to - from);
                    maxWait = Math.max(maxWait, to - from);
                } catch (IOException e) {
                    try {
                        pipes.remove(pipe);
                        pipe.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    i--;
                }
            }
            if (minWait > 1 && maxWait > 1 && pipes.size() != 0)
                System.out.println(minWait + " " + maxWait + " " + pipes.size());
        }
    }

    public static class Pipe {
        InputStream inC;
        OutputStream outC;
        private Socket s;
        InputStream inS;
        OutputStream outS;

        public Pipe(InputStream inS, OutputStream outS, InputStream inC, OutputStream outC, Socket s) {
            this.inS = inS;
            this.outS = outS;
            this.inC = inC;
            this.outC = outC;
            this.s = s;
        }

        private void close() throws IOException {
            inC.close();
            outC.close();
            inS.close();
            outS.close();
        }
    }
}
