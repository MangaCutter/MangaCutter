package net.macu.browser.proxy;

import net.macu.browser.proxy.server.HTTPSPipe;
import net.macu.util.UnblockableBufferedReader;

import javax.imageio.ImageIO;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Handler extends Thread {
    private static SocketFactory socketFactory = null;
    private static SSLSocketFactory sslSocketFactory = null;
    private static int Counter = 0;
    protected final InputStream browserInputStream;
    private final boolean secure;
    protected Socket targetSocket;
    protected UnblockableBufferedReader browserReader;
    protected PrintWriter browserWriter;
    protected final OutputStream browserOutputStream;
    private final CapturedImageProcessor capturedImages;
    protected UnblockableBufferedReader targetReader;
    protected PrintWriter targetWriter;
    private OutputStream targetStream;
    private String protocolVersion = "HTTP/1.1";
    protected boolean keepAlive = false;
    private int keepAliveMax = 100;
    private int keepAliveRequestCount = 0;
    private long keepAliveTimeout = 6000;
    private long keepAliveLastTime = 0;
    private String lastTargetHost = "";
    private int lastTargetPort = -1;
    protected static final int BREAK_EXIT_CODE = 1;
    protected static final int RETURN_EXIT_CODE = -1;
    protected static final int OK_EXIT_CODE = 0;

    public Handler(InputStream in, OutputStream out, boolean secure, CapturedImageProcessor capturedImages) {
        browserInputStream = in;
        browserOutputStream = out;
        this.capturedImages = capturedImages;
        setDaemon(true);
        setName("Handler-" + (Counter));
        Counter++;
        this.secure = secure;
    }

    protected int handle() throws IOException {
        if (keepAlive) {
            if (keepAliveMax != -1 && keepAliveMax <= keepAliveRequestCount) {
                return BREAK_EXIT_CODE;
            } else {
                keepAliveRequestCount++;
            }
            while (keepAliveTimeout >= System.currentTimeMillis() - keepAliveLastTime) {
                if (browserReader.available() == 0) {
                    Thread.yield();
                } else {
                    break;
                }
            }
            if (keepAliveTimeout != -1 && keepAliveTimeout < System.currentTimeMillis() - keepAliveLastTime) {
                System.out.println(Thread.currentThread().getName() + " timeout");
                return BREAK_EXIT_CODE;
            }
        }
        String requestLine = browserReader.readLine(false);
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length != 3) {
            sendErrorStatusMessage(400, "Bad Request");
            return BREAK_EXIT_CODE;
        }
        protocolVersion = requestParts[2];
        if (!protocolVersion.equals("HTTP/1.1") && !protocolVersion.equals("HTTP/1")) {
            sendErrorStatusMessage(400, "Bad Request");
            return BREAK_EXIT_CODE;
        }
        String requestMethod = requestParts[0];
        String requestUrl = requestParts[1];

        //read request headers
        String targetHost = "";
        int targetPort = secure ? 443 : 80;
        int requestContentLength = 0;
        String requestEncoding = "identity";
        ArrayList<Header> requestHeaders = new ArrayList<>();
        String buffer;
        while ((buffer = browserReader.readLine(false)) != null && !buffer.isEmpty()) {
            Header h = new Header(buffer);
            if (h.headerName.equals("Accept-Encoding") ||
                    h.headerName.equals("If-Modified-Since") ||
                    h.headerName.equals("If-None-Match")) {
                continue;
            }
            if (h.headerName.equals("Host")) {
                int colonIndex = h.headerData.indexOf(":");
                if (colonIndex != -1) {
                    targetHost = h.headerData.substring(0, colonIndex);
                    targetPort = Integer.parseInt(h.headerData.substring(colonIndex + 1));
                } else {
                    targetHost = h.headerData;
                }
            }
            if (h.headerName.equals("Content-Length")) {
                requestContentLength = Integer.parseInt(h.headerData);
                continue;
            }
            if (h.headerName.equals("Connection")) {
                keepAlive = h.headerData.equals("keep-alive");
            }
            if (h.headerName.equals("Keep-Alive")) {
                String[] params = h.headerData.split(",");
                for (String p :
                        params) {
                    p = p.trim();
                    String[] par = p.split("=");
                    switch (par[0]) {
                        case "timeout":
                            keepAliveTimeout = Long.parseLong(par[1]) * 1000;
                            break;
                        case "max":
                            keepAliveMax = Integer.parseInt(par[1]);
                            break;
                    }
                }
            }
            if (h.headerName.equals("Transfer-Encoding")) {
                requestEncoding = h.headerData;
                h.headerData = "identity";
            }
            requestHeaders.add(h);
        }
        byte[] requestBody;

        //read request body
        if (requestEncoding.equals("chunked")) {
            requestBody = readChunkedBody(browserReader);
        } else if (requestEncoding.equals("identity")) {
            requestBody = readFixedSizeBody(requestContentLength, browserReader);
        } else {
            throw new UnsupportedOperationException("Unsupported encoding type: " + requestEncoding);
        }
        requestHeaders.add(new Header("Accept-Encoding", "identity"));
        requestHeaders.add(new Header("Content-Length", String.valueOf(requestBody.length)));
        if (requestMethod.equals("CONNECT")) {
            HTTPSPipe.pipe(browserReader, browserOutputStream, targetHost, capturedImages);
            System.out.println(Thread.currentThread().getName() + " stopped");
            return RETURN_EXIT_CODE;
        }

        if (!lastTargetHost.equals(targetHost) || lastTargetPort != targetPort) {
            if (targetSocket != null) {
                targetWriter.close();
                targetReader.close();
                targetSocket.close();
            }
            targetSocket = createSocket(targetHost, targetPort, secure);
            targetStream = targetSocket.getOutputStream();
            targetWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(targetStream)));
            targetReader = new UnblockableBufferedReader(targetSocket.getInputStream());
            String targetAddress = targetSocket.getRemoteSocketAddress().toString();
            targetPort = Integer.parseInt(targetAddress.substring(targetAddress.lastIndexOf(":") + 1));
        }
        sendMessage(requestMethod + " " + requestUrl + " " + protocolVersion, requestHeaders, requestBody, targetWriter, targetStream);

        //read target response
        String responseLine = targetReader.readLine(false);
        String responseMethod = responseLine.substring(0, responseLine.indexOf(" "));
        responseLine = responseLine.substring(responseLine.indexOf(" ") + 1);
        int responseCode = Integer.parseInt(responseLine.substring(0, responseLine.indexOf(" ")));
        String responseDescription = responseLine.substring(responseLine.indexOf(" ") + 1);
        ArrayList<Header> responseHeaders = new ArrayList<>();
        int responseContentLength = 0;
        String responseEncoding = "identity";
        while ((buffer = targetReader.readLine(false)) != null && !buffer.isEmpty()) {
            Header h = new Header(buffer);
            if (h.headerName.equals("Content-Length")) {
                responseContentLength = Integer.parseInt(h.headerData);
                continue;
            }
            if (h.headerName.equals("Connection")) {
                keepAlive = h.headerData.equals("keep-alive");
            }
            if (h.headerName.equals("Transfer-Encoding")) {
                responseEncoding = h.headerData;
                h.headerData = "identity";
            }
            responseHeaders.add(h);
        }
        byte[] responseBody;
        if (responseEncoding.equals("chunked")) {
            responseBody = readChunkedBody(targetReader);
        } else if (responseEncoding.equals("identity")) {
            responseBody = readFixedSizeBody(responseContentLength, targetReader);
        } else {
            sendErrorStatusMessage(400, "Bad Request");
            return BREAK_EXIT_CODE;
        }
        responseHeaders.add(new Header("Content-Length", String.valueOf(responseBody.length)));
        sendMessage(responseMethod + " " + responseCode + " " + responseDescription, responseHeaders, responseBody, browserWriter, browserOutputStream);
        try {
            BufferedImage interceptedImage = ImageIO.read(new ByteArrayInputStream(responseBody));
            if (interceptedImage != null) {
                if (isURLValid(requestUrl))
                    capturedImages.putImage(requestUrl, interceptedImage);//todo change image signature
                else if (isURLValid("http" + (secure ? "s" : "") + "://" + targetHost + requestUrl)) {
                    capturedImages.putImage("http" + (secure ? "s" : "") + "://" + targetHost + requestUrl, interceptedImage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastTargetPort = targetPort;
        lastTargetHost = targetHost;
        keepAliveLastTime = System.currentTimeMillis();
        return OK_EXIT_CODE;
    }

    private static Socket createSocket(String host, int port, boolean secure) throws IOException {
        if (secure) {
            if (sslSocketFactory == null)
                sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
            socket.startHandshake();
            return socket;
        } else {
            if (socketFactory == null)
                socketFactory = SocketFactory.getDefault();
            return socketFactory.createSocket(host, port);
        }
    }

    private static byte[] readFixedSizeBody(int contentLength, UnblockableBufferedReader bodyStream) throws IOException {
        byte[] body = new byte[contentLength];
        for (int filled = 0; filled < contentLength; ) {
            filled += bodyStream.read(body, filled, contentLength - filled);
        }
        return body;
    }

    private static byte[] readChunkedBody(UnblockableBufferedReader bodyStream) throws IOException {
        String buffer;
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        while ((buffer = bodyStream.readLine(false)) != null && !buffer.isEmpty()) {
            int size = Integer.parseInt(buffer, 16);
            if (size == 0) {
                bodyStream.readLine(true);
                break;
            }
            byte[] chunk = new byte[size];
            for (int filled = 0; filled < size; ) {
                filled += bodyStream.read(chunk, filled, size - filled);
            }
            body.write(chunk);
            bodyStream.readLine(true);
        }
        return body.toByteArray();
    }

    private static void sendMessage(String firstLine, List<Header> headers, byte[] body, PrintWriter writer, OutputStream rawOutput) throws IOException {
        writer.print(firstLine + "\r\n");
        headers.forEach(header -> writer.print(header.toString() + "\r\n"));
        writer.print("\r\n");
        writer.flush();
        if (body.length != 0) {
            rawOutput.write(body);
            rawOutput.flush();
        }
    }

    private static boolean isURLValid(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " started");
        try {
            browserReader = new UnblockableBufferedReader(browserInputStream);
            browserWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(browserOutputStream)));
            handle_cycle:
            do {
                int retCode = handle();
                switch (retCode) {
                    case OK_EXIT_CODE:
                        break;
                    case BREAK_EXIT_CODE:
                        break handle_cycle;
                    case RETURN_EXIT_CODE:
                        return;
                }
            } while (keepAlive);
            browserWriter.close();
            browserReader.close();
            targetWriter.close();
            targetReader.close();
            targetSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " stopped");
    }

    private void sendErrorStatusMessage(int code, String description) {
        browserWriter.print(protocolVersion);
        browserWriter.print(" ");
        browserWriter.print(code);
        browserWriter.print(" ");
        browserWriter.print(description);
        browserWriter.print("\r\n\r\n");
        browserWriter.flush();
    }
}
