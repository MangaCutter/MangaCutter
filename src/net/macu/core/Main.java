package net.macu.core;

import net.macu.UI.Form;
import net.macu.UI.IconManager;
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

public class Main {
    private static List<Form> forms;
    private static List<Service> services;
    private static final int VERSION_MINOR = 4;
    private static Reflections reflections;

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, "Error: " + sw.toString());
        });

        reflections = new Reflections("net.macu");

        Settings.collectParameters();
        L.loadLanguageData();

        Security.addProvider(new BouncyCastleProvider());
        CertificateAuthority.loadRootCA();

        ViewManager.setLookAndFeel();

        BrowserPlugin.getPlugin().start();

        Set<Class<? extends Form>> formsSet = reflections.getSubTypesOf(Form.class);
        forms = Collections.unmodifiableList(getInstances(formsSet));

        Set<Class<? extends Service>> servicesSet = reflections.getSubTypesOf(Service.class);
        services = Collections.unmodifiableList(getInstances(servicesSet));

        IOManager.checkUpdates();
        IOManager.initClient();

        IconManager.loadIcons();
        ViewManager.createView();
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
        return "v" + forms.size() + "." + services.size() + "." + VERSION_MINOR;
    }
}
