package net.macu.browser.proxy.server;

import net.macu.browser.proxy.CapturedImageProcessor;
import net.macu.browser.proxy.Handler;
import net.macu.util.UnblockableBufferedReader;
import org.bouncycastle.tls.TlsServerProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HTTPSPipe {

    public static void pipe(UnblockableBufferedReader in, OutputStream out, String targetHost, CapturedImageProcessor capturedImages) {
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
