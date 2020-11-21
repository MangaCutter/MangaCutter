package net.ddns.masterlogick.core;

import net.ddns.masterlogick.UI.Form;
import net.ddns.masterlogick.UI.MainView;
import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.service.Service;
import org.reflections.Reflections;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Main {
    private static List<Form> forms;
    private static List<Service> services;
    private static final int VERSION_MINOR = 2;

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
            JOptionPane.showMessageDialog(null, "Ошибка: " + sw.toString());
        });
        Reflections rf = new Reflections(Form.class.getPackage().getName());
        Set<Class<? extends Form>> formsSet = rf.getSubTypesOf(Form.class);
        formsSet.removeIf(aClass -> aClass.equals(MainView.class));
        forms = Collections.unmodifiableList(getInstances(formsSet));

        rf = new Reflections(Service.class.getPackage().getName());
        Set<Class<? extends Service>> servicesSet = rf.getSubTypesOf(Service.class);
        services = Collections.unmodifiableList(getInstances(servicesSet));

        IOManager.checkUpdates();

        IOManager.initClient();
        ViewManager.createView();
    }

    private static <T> List<T> getInstances(Set<Class<? extends T>> classSet) {
        ArrayList<T> instancesList = new ArrayList();
        classSet.forEach(aClass -> {
            try {
                instancesList.add(aClass.getConstructor(null).newInstance(null));
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

    public static String getVersion() {
        return "v" + forms.size() + "." + services.size() + "." + VERSION_MINOR;
    }
}
