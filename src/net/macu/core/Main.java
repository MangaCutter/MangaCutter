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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
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

        IOManager.initClient();
        IOManager.checkUpdates(false);

        ViewManager.initFileChoosers();

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
        return "v5.1.1";
    }
}
