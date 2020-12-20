package net.macu.browser.image_proxy.proxy;

import net.macu.util.UnblockableBufferedReader;
import org.bouncycastle.tls.TlsServerProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class HTTPSPipe {

    public static void pipe(UnblockableBufferedReader in, OutputStream out, String targetHost) {
        try {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            TlsServerProtocol proto = new TlsServerProtocol(in, out);
            System.out.println(Thread.currentThread().getName() + " started tls handshake at time: " + Calendar.getInstance().getTime().toString());
            TlsServerImpl impl = new TlsServerImpl(targetHost);
            System.out.println(Thread.currentThread().getName() + " tls server created at time: " + Calendar.getInstance().getTime().toString());
            proto.accept(impl);
            System.out.println(Thread.currentThread().getName() + " ended tls handshake at time: " + Calendar.getInstance().getTime().toString());
            new Handler(proto.getInputStream(), proto.getOutputStream(), true).start();
        } catch (Exception e) {
            System.out.print(Thread.currentThread().getName() + " ");
            e.printStackTrace();
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
