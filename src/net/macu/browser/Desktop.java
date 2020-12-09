package net.macu.browser;

import java.io.IOException;

public class Desktop {
    public static void openUrlInDefaultBrowser(String url) throws IOException {
        Runtime rt = Runtime.getRuntime();
        switch (getOSType()) {
            case Mac:
                rt.exec(new String[]{"open", url});
                return;
            case Win:
                rt.exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                return;
            case Unix:
                rt.exec(new String[]{"xdg-open", url});
                break;
//            case Unknown:
//                throw new IOException(L.get("browser.Desktop.openUrlInDefaultBrowser.unknownOS"));
        }
    }

    private static OS getOSType() {
        String os = System.getProperty("os.name");
        if (os.contains("win")) return OS.Win;
        if (os.contains("mac")) return OS.Mac;
        if (os.contains("nix") || os.contains("nux")) return OS.Unix;
        return OS.Unknown;
    }

    private enum OS {
        Unix, Win, Mac, Unknown
    }
}
