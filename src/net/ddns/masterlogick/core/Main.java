package net.ddns.masterlogick.core;

import net.ddns.masterlogick.UI.Form;
import net.ddns.masterlogick.UI.MainView;
import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.service.IOManager;
import net.ddns.masterlogick.service.Service;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Main {
    public static final String VERSION = "v2.1.0";
    private static List<Form> forms;
    private static List<Service> services;

    public static void main(String[] args) {
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
}
