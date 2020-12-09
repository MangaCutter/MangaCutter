package net.macu.browser.image_proxy.proxy;

import net.macu.util.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HTTPSPipe extends Thread {

    public static void pipe(BufferedReader in, OutputStream out, Socket client) {
        try {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            e.printStackTrace();
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
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    if (in.available()) {
                        int received = in.read(buffer);
                        if (received < 0) break;
                        dOut.write(buffer, 0, received);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                try {
                    if (dIn.available() != 0) {
                        int sent = dIn.read(buffer);
                        if (sent < 0) break;
                        out.write(buffer, 0, sent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            try {
                dIn.close();
                dOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
