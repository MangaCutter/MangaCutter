package net.macu.browser;

import com.jogamp.opengl.GLProfile;
import net.macu.core.IOManager;
import org.cef.CefApp;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.SystemBootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JCefLoader {
    public static final String JAVA_LIBRARY_PATH = "java.library.path";
    public static final String LOCK_FILE_NAME = "install.lock";
    public static final String NATIVE_BINARIES_JAR_URI = "https://github.com/MangaCutter/jcefbuild/releases/download/v1.0.0/%s.zip";
    private static boolean loaded = false;

    public static synchronized void install(File installDir) throws IOException {
        if (installDir.exists() && !installDir.isDirectory()) {
            throw new RuntimeException(installDir.getAbsolutePath() + " already exists and it's a file");
        }
        if (!installDir.exists()) installDir.mkdirs();
        if (new File(installDir, LOCK_FILE_NAME).exists()) {
            throw new IllegalStateException("Directory already contains bundle persisting file " + LOCK_FILE_NAME);
        }
        String path = String.format(NATIVE_BINARIES_JAR_URI, OSUtil.getOSIdentifier());
        System.out.println("obtaining " + path);
        InputStream in = IOManager.sendRawRequest(path).getContent();
        ZipInputStream zipInputStream = new ZipInputStream(in);
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.isDirectory() || zipEntry.getName().isEmpty())
                continue;
            File out = new File(installDir, zipEntry.getName());
            out.getParentFile().mkdirs();
            out.createNewFile();
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buffer = new byte[1024 * 512];
            for (int len = zipInputStream.read(buffer); len != -1; len = zipInputStream.read(buffer)) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            //Make certain files executable (under linux and macosx)
            if (OS.isLinux() || OS.isMacintosh()) {
                if (out.getName().endsWith(File.separator + "jcef_helper")) {
                    out.setExecutable(true);
                }
            }
            zipInputStream.closeEntry();
        }
        zipInputStream.close();
        //Write install lock to indicate success for future startups
        File locker = new File(installDir, LOCK_FILE_NAME);
        locker.createNewFile();
        FileOutputStream fos = new FileOutputStream(locker);
        fos.write(path.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    public static synchronized CefApp loadCef(File installDir, CefSettings settings, String... args)
            throws RuntimeException {
        //Dont load twice
        if (loaded) return CefApp.getInstance();
        loaded = true;
        if (!new File(installDir, LOCK_FILE_NAME).exists()) {
            throw new RuntimeException("Invalid bundle setup");
        }
        //Only bundle is supported for macosx, no need to install
        if (OS.isMacintosh()) {
            boolean success = CefApp.startup(args);
            if (!success)
                throw new RuntimeException("Well, pls don't use Mac OS :) Or push commit in project repo to add Mac OS support");
            return CefApp.getInstance(args, settings);
        }

        //Patch java library path to scan the install dir of our application
        //This is required for jcef to find all resources
        String path = System.getProperty(JAVA_LIBRARY_PATH);
        if (!path.endsWith(File.pathSeparator)) path += File.pathSeparator;
        path += installDir.getAbsolutePath();
        System.setProperty(JAVA_LIBRARY_PATH, path);
        SystemBootstrap.setLoader(libname -> {
        });
//        if (OS.isWindows()) {
        GLProfile glp = GLProfile.getMaxFixedFunc(true);
//        }
//        Toolkit.getDefaultToolkit().getDesktopProperty("");

        //Load native libraries for jcef, as the jvm does not update the java library path
        System.loadLibrary("jawt");
        if (OS.isWindows()) {
            System.load(new File(installDir, "d3dcompiler_47.dll").getAbsolutePath());
            System.load(new File(installDir, "libGLESv2.dll").getAbsolutePath());
            System.load(new File(installDir, "libEGL.dll").getAbsolutePath());
            System.load(new File(installDir, "chrome_elf.dll").getAbsolutePath());
            System.load(new File(installDir, "libcef.dll").getAbsolutePath());
            System.load(new File(installDir, "jcef.dll").getAbsolutePath());
        } else if (OS.isLinux()) {
            //Make jcef_helper executable
            File jcef_helper = new File(installDir, "jcef_helper");
            jcef_helper.setExecutable(true);
            //Load jcef native library
            System.load(new File(installDir, "libjcef.so").getAbsolutePath());
            //Initialize cef
            boolean success = CefApp.startup(args);
            if (!success)
                throw new RuntimeException("JCef did not initialize correctly!");
            System.load(new File(installDir, "libcef.so").getAbsolutePath());
        }
        //Configure cef settings and create app instance (currently nothing to configure, may change in the future)
        return CefApp.getInstance(args, settings);
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
