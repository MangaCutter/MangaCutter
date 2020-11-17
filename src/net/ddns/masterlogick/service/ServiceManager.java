package net.ddns.masterlogick.service;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

public class ServiceManager {
    public static Service getService(String uri) {
        if (uri.isEmpty()) return null;
        Reflections rf = new Reflections(Service.class.getPackage().getName());
        for (Class<? extends Service> c : rf.getSubTypesOf(Service.class)) {
            try {
                if ((boolean) c.getMethod("parsePage", String.class).invoke(null, uri)) {
                    try {
                        return c.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        //todo process exception
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        //todo process exception
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                //todo process exception
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                //todo process exception
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                //todo process exception
            }
        }
        return null;
    }

    public static String getSupportedServicesList() {
        Reflections rf = new Reflections(Service.class.getPackage().getName());
        Iterator<Class<? extends Service>> it = rf.getSubTypesOf(Service.class).iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            try {
                sb.append(it.next().getDeclaredMethod("getInfo").invoke(null)).append("\n");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                //todo process exception
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                //todo process exception
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                //todo process exception
            }
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
