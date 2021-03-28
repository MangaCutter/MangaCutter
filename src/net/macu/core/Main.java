package net.macu.core;

import net.macu.UI.Form;
import net.macu.UI.IconManager;
import net.macu.UI.MainView;
import net.macu.UI.ViewManager;
import net.macu.browser.plugin.BrowserPlugin;
import net.macu.browser.proxy.cert.CertificateAuthority;
import net.macu.service.Service;
import net.macu.settings.L;
import net.macu.settings.Settings;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.reflections.Reflections;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;

public class Main {
    private static List<Form> forms;
    private static List<Service> services;
    private static Reflections reflections;

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void main(String[] args) {
        //preparations for main() execution
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Info");

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, "Error: " + sw.toString());
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Settings.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }));

        loadNativeFromJar();

        reflections = new Reflections("net.macu");

        Settings.loadSettings();
        L.loadLanguageData();
        IconManager.loadIcons();

        ViewManager.setLookAndFeel();

        Security.addProvider(new BouncyCastleProvider());
        CertificateAuthority.loadRootCA();

        Set<Class<? extends Form>> formsSet = reflections.getSubTypesOf(Form.class);
        forms = Collections.unmodifiableList(getInstances(formsSet));
        Set<Class<? extends Service>> servicesSet = reflections.getSubTypesOf(Service.class);
        services = Collections.unmodifiableList(getInstances(servicesSet));

        ViewManager.initFileChoosers();

        //real main() start here

        IOManager.initClient();
        IOManager.checkUpdates(false);

        new MainView();

        if (CertificateAuthority.getRootCA() != null)
            BrowserPlugin.getPlugin().start();
    }

    private static <T> List<T> getInstances(Set<Class<? extends T>> classSet) {
        ArrayList<T> instancesList = new ArrayList();
        classSet.forEach(aClass -> {
            try {
                instancesList.add(aClass.getConstructor().newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
        return instancesList;
    }

    public static List<Form> getForms() {
        return forms;
    }

    public static List<Service> getServices() {
        return services;
    }

    public static Reflections getReflections() {
        return reflections;
    }

    public static String getVersion() {
        return "v5.2.1";
    }

    //from com.luciad.imageio.webp.NativeLibraryUrls
    public static void loadNativeFromJar() {
        String os = System.getProperty("os.name").toLowerCase();
        String bits = System.getProperty("os.arch").contains("64") ? "64" : "32";
        String libFilename = "libwebp-imageio.so";
        String platform = "linux";
        if (os.contains("win")) {
            platform = "win";
            libFilename = "webp-imageio.dll";
        } else if (os.contains("mac")) {
            platform = "mac";
            libFilename = "libwebp-imageio.dylib";
        }

        try {
            InputStream in = Main.class.getResourceAsStream(String.format("/native/%s/%s/%s", platform, bits, libFilename));
            Throwable var5 = null;

            try {
                if (in == null) {
                    throw new RuntimeException(String.format("Could not find WebP native library for %s %s in the jar", platform, bits));
                }

                File tmpLibraryFile = Files.createTempFile("", libFilename).toFile();
                tmpLibraryFile.deleteOnExit();
                FileOutputStream out = new FileOutputStream(tmpLibraryFile);
                Throwable var8 = null;

                try {
                    byte[] buffer = new byte[8192];

                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                } catch (Throwable var34) {
                    var8 = var34;
                    throw var34;
                } finally {
                    if (out != null) {
                        if (var8 != null) {
                            try {
                                out.close();
                            } catch (Throwable var33) {
                                var8.addSuppressed(var33);
                            }
                        } else {
                            out.close();
                        }
                    }

                }

                System.load(tmpLibraryFile.getAbsolutePath());
            } catch (Throwable var36) {
                var5 = var36;
                throw var36;
            } finally {
                if (in != null) {
                    if (var5 != null) {
                        try {
                            in.close();
                        } catch (Throwable var32) {
                            var5.addSuppressed(var32);
                        }
                    } else {
                        in.close();
                    }
                }

            }

        } catch (IOException var38) {
            throw new RuntimeException("Could not load native WebP library", var38);
        }
    }
}
