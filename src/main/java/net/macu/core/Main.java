package net.macu.core;

import net.macu.UI.*;
import net.macu.browser.plugin.BrowserPlugin;
import net.macu.browser.proxy.cert.CertificateAuthority;
import net.macu.service.*;
import net.macu.settings.L;
import net.macu.settings.Settings;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    private static List<Form> forms;
    private static List<Service> services;

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void main(String[] args) {
        prepareContext();
        IOManager.checkUpdates(false);
        new MainView();
        if (CertificateAuthority.getRootCA() != null)
            BrowserPlugin.getPlugin().start();
    }

    private static void prepareContext() {
        System.setProperty("defaultLogLevel", "Info");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, "Error: " + sw);
        });
        Settings.loadSettings();
        L.loadLanguageData();
        IconManager.loadIcons();
        ViewManager.setLookAndFeel();
        Security.addProvider(new BouncyCastleProvider());
        CertificateAuthority.loadRootCA();
        forms = Collections.unmodifiableList(Arrays.asList(new AsIsPageForm(), new ManualForm(), new SinglePageForm(),
                new ConstantHeightForm(), new MultiPageForm()));
        services = Collections.unmodifiableList(Arrays.asList(new AcQq(), new Daum(), new LocalFiles(), new ManhuaGui(),
                new Naver(), new Rawdevart()));
        ViewManager.initFileChoosers();
        IOManager.initClient();
    }

    public static List<Form> getForms() {
        return forms;
    }

    public static List<Service> getServices() {
        return services;
    }

    public static String getVersion() {
        return "v5.3.1";
    }
}
