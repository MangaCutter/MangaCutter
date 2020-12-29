package net.macu.browser.image_proxy.proxy;

import net.macu.browser.image_proxy.CapturedImageMap;
import net.macu.util.UnblockableBufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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

    public static synchronized void pipe(UnblockableBufferedReader in, OutputStream out, String targetHost, CapturedImageMap capturedImages) {
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
        SocketChannel s = null;
        try {
            s = SocketChannel.open();
            s.connect(new InetSocketAddress("127.0.0.1", 50002));
            s.finishConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (s != null) {
//            InputStream dIn = null;
//            OutputStream dOut = null;
//            try {
//                dIn = s.getInputStream();
//                dOut = s.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            pipes.add(new Pipe(s, in, out));
//            pipes.add(new Pipe(in, dOut));
        } else {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024 * 16];
        ByteBuffer buffer1 = ByteBuffer.allocate(1024 * 16);
        while (true) {
            long minWait = Long.MAX_VALUE;
            long maxWait = 0;
            if (pipes.size() == 0) Thread.yield();
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                try {
                    long from = System.currentTimeMillis();
                    if (pipe.handlerChannel.isOpen()) {
                        if (pipe.inC.available() > 0) {
                            int read = pipe.inC.read(buffer);
                            pipe.handlerChannel.write(ByteBuffer.wrap(buffer, 0, read));
                        }
                        int read = pipe.handlerChannel.read(buffer1);
                        pipe.outC.write(buffer1.array(), 0, read);
                    } else {
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
            if (minWait != 0 && maxWait != 0 && pipes.size() != 0)
                System.out.println(minWait + " " + maxWait + " " + pipes.size());
        }
    }

    private static class Pipe {
        private final SocketChannel handlerChannel;
        InputStream inC;
        OutputStream outC;

        private Pipe(SocketChannel handlerChannel, InputStream inC, OutputStream outC) {
            this.handlerChannel = handlerChannel;
            this.inC = inC;
            this.outC = outC;
        }

        private void close() throws IOException {
            inC.close();
            outC.close();
            handlerChannel.close();
        }
    }
}
