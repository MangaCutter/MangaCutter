package net.macu.browser.interceptor;

import net.macu.browser.PostDataInputStream;
import net.macu.core.IOManager;
import org.apache.http.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicRequestLine;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Future;

public class InterceptingResourceHandler implements CefResourceHandler {
    private static final String schemeSeparator = "://";
    private static final String hostSeparator = "/";
    private static final String portSeparator = ":";
    private static final String fragmentSeparator = "#";
    private final int browserId;
    private Future<HttpResponse> processingFuture;
    private HttpResponse httpResponse;
    private InputStream content;
    private String url;

    public InterceptingResourceHandler(int browserId) {
        this.browserId = browserId;
    }

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        url = request.getURL();
        String encodedRequest = request.getURL();
        String scheme = "";
        if (encodedRequest.contains(schemeSeparator)) {
            scheme = encodedRequest.substring(0, encodedRequest.indexOf(schemeSeparator));
        }
        String ssp = encodedRequest.substring(scheme.length() + (encodedRequest.contains(schemeSeparator) ? 1 : 0));
        String fragment = null;
        if (ssp.contains(fragmentSeparator)) {
            fragment = ssp.substring(ssp.lastIndexOf(fragmentSeparator) + 1);
            ssp = ssp.substring(0, ssp.lastIndexOf(fragmentSeparator));
        }
        try {
            encodedRequest = new URI(scheme, ssp, fragment).toASCIIString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        scheme = "";
        if (encodedRequest.contains(schemeSeparator))
            scheme = encodedRequest.substring(0, encodedRequest.indexOf(schemeSeparator));
        String host = encodedRequest.substring(scheme.length() + (encodedRequest.contains(schemeSeparator) ? schemeSeparator.length() : 0));
        String query = "/";
        if (encodedRequest.contains(hostSeparator)) {
            query = host.substring(host.indexOf(hostSeparator));
            host = host.substring(0, host.indexOf(hostSeparator));
        }

        int port = -1;
        if (host.contains(portSeparator)) {
            port = Integer.parseInt(host.substring(host.indexOf(portSeparator) + 1));
            host = host.substring(0, host.indexOf(portSeparator));
        }
        RequestLine requestLine = new BasicRequestLine(request.getMethod(), query, HttpVersion.HTTP_1_1);
        HttpRequest realRequest = null;
        if (request.getPostData() != null) {
            Vector<CefPostDataElement> postDataArray = new Vector<>();
            request.getPostData().getElements(postDataArray);
            if (!postDataArray.isEmpty() && postDataArray.get(0).getType() != CefPostDataElement.Type.PDE_TYPE_EMPTY) {
                CefPostDataElement postEntity = postDataArray.get(0);
                realRequest = new BasicHttpEntityEnclosingRequest(requestLine);
                if (postEntity.getType() == CefPostDataElement.Type.PDE_TYPE_BYTES) {
                    InputStreamEntity entity = new InputStreamEntity(new PostDataInputStream(postEntity),
                            postEntity.getBytesCount());
                    ((HttpEntityEnclosingRequest) realRequest).setEntity(entity);
                } else {
                    FileEntity entity = new FileEntity(new File(postEntity.getFile()));
                    ((HttpEntityEnclosingRequest) realRequest).setEntity(entity);
                }
            }
        }
        if (realRequest == null) {
            realRequest = new BasicHttpRequest(requestLine);
        }
        HashMap<String, String> headers = new HashMap<>();
        request.getHeaderMap(headers);
        headers.forEach(realRequest::addHeader);
        HttpHost httpHost = new HttpHost(host, port, scheme);
        try {
            processingFuture = IOManager.sendRawAsyncRequest(httpHost, realRequest, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    httpResponse = result;
                    if (result.getEntity() != null)
                        try {
                            content = result.getEntity().getContent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    callback.Continue();
                }

                @Override
                public void failed(Exception ex) {
                    ex.printStackTrace();
                    callback.cancel();
                }

                @Override
                public void cancelled() {
                    callback.cancel();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
        Header redirectHeader = httpResponse.getFirstHeader("location");
        if (redirectHeader != null) {
            redirectUrl.set(redirectHeader.getValue());
            return;
        }
        StatusLine statusLine = httpResponse.getStatusLine();
        response.setStatus(statusLine.getStatusCode());
        response.setStatusText(statusLine.getReasonPhrase());
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            if (entity.getContentType() != null) {
                String mimeType = entity.getContentType().getValue();
                for (String type : ImageIO.getReaderMIMETypes()) {
                    if (mimeType.contains(type)) {
                        try {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024 * 4];
                            for (int i = content.read(buffer); i != -1; i = content.read(buffer)) {
                                baos.write(buffer, 0, i);
                            }
                            byte[] imageData = baos.toByteArray();
                            content = new ByteArrayInputStream(imageData);
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                            Interceptor.addImage(browserId, url, img);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                if (mimeType.contains(";"))
                    mimeType = mimeType.substring(0, mimeType.indexOf(";"));
                response.setMimeType(mimeType);
            } else response.setMimeType("application/octet-stream");
            responseLength.set((int) Math.max(-1, entity.getContentLength()));
        } else {
            response.setMimeType("application/octet-stream");
        }
        HashMap<String, String> headerMap = new HashMap<>();
        Arrays.asList(httpResponse.getAllHeaders()).forEach(header -> headerMap.put(header.getName(), header.getValue()));
        response.setHeaderMap(headerMap);
    }

    @Override
    public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) {
        try {
            int read = content.read(dataOut, 0, bytesToRead);
            if (read == -1) {
                return false;
            }
            bytesRead.set(read);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void cancel() {
        processingFuture.cancel(true);
    }
}
