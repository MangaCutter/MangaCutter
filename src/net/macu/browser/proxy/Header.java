package net.macu.browser.proxy;

public class Header {
    String headerName;
    String headerData;

    public Header(String headerName, String headerData) {
        this.headerData = headerData;
        this.headerName = headerName;
    }

    public Header(String rawHeaderLine) {
        int colonIndex = rawHeaderLine.indexOf(":");
        headerName = rawHeaderLine.substring(0, colonIndex);
        headerData = rawHeaderLine.substring(colonIndex + 2);
    }

    @Override
    public String toString() {
        return headerName + ": " + headerData;
    }
}
