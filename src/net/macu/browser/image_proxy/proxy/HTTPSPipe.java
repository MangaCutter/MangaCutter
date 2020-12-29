package net.macu.browser.image_proxy.proxy;

import net.macu.browser.image_proxy.CapturedImageMap;
import net.macu.util.UnblockableBufferedReader;
import org.bouncycastle.tls.TlsServerProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HTTPSPipe {

    public static void pipe(UnblockableBufferedReader in, OutputStream out, String targetHost, CapturedImageMap capturedImages) {
        try {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            TlsServerProtocol proto = new TlsServerProtocol(in, out);
            TlsServerImpl impl = new TlsServerImpl(targetHost);
            proto.accept(impl);
            new Handler(proto.getInputStream(), proto.getOutputStream(), true, capturedImages).start();
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
