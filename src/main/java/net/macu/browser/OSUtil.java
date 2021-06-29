package net.macu.browser;

import com.jogamp.common.os.Platform;
import org.cef.OS;

public class OSUtil {
    public static String getOsName() throws RuntimeException {
        if (OS.isLinux()) return "linux";
        else if (OS.isMacintosh()) return "macosx";
        else if (OS.isWindows()) return "win";
        else
            throw new RuntimeException("Unknown operating system: " + System.getProperty("os.name"));
    }

    public static String getJvmArch() {
        if (Platform.is64Bit()) return "64";
        else return "32";
    }

    public static String getOSIdentifier() {
        return getOsName() + getJvmArch();
    }
}