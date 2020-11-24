package net.macu.settings;


import net.macu.UI.ViewManager;
import net.macu.core.Main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {
    private static final ArrayList<Parameters> parametersLsit = new ArrayList<>();
    static Preferences preferences;
    private static Properties defaults;

    public static void collectParameters() {
        if (preferences != null) return;
        preferences = Preferences.userNodeForPackage(Settings.class);
        Main.getReflections().getSubTypesOf(Parametrized.class).forEach(aClass -> {
            try {
                parametersLsit.add((Parameters) aClass.getMethod("getParameters").invoke(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
        defaults = new Properties();
        try {
            defaults.load(Settings.class.getResourceAsStream("defaultSettings.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            ViewManager.showMessageDialog(L.get("settings.Settings.collectParameters.get_defaults_exception", e.toString()));
        }
        defaults.stringPropertyNames().forEach(s -> {
            try {
                if (!Arrays.asList(preferences.keys()).contains(s)) {
                    preferences.put(s, defaults.getProperty(s));
                }
            } catch (BackingStoreException e) {
                e.printStackTrace();
                ViewManager.showMessageDialog(L.get("settings.Settings.collectParameters.put_defaults_exception", e.toString()));
            }
        });
        parametersLsit.forEach(parameters -> parameters.forEach(parameter -> {
            switch (parameter.getType()) {
                case BOOLEAN_TYPE:
                    parameter.setValue(preferences.getBoolean(parameter.getName(), false));
                    break;
                case STRING_TYPE:
                    parameter.setValue(preferences.get(parameter.getName(), ""));
                    break;
                case INT_TYPE:
                    parameter.setValue(preferences.getInt(parameter.getName(), Integer.MIN_VALUE));
                    break;
            }
        }));
    }

    public static List<Parameters> getAllParameters() {
        return parametersLsit;
    }
}
