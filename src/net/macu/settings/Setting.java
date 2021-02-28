package net.macu.settings;

import java.util.prefs.BackingStoreException;

public abstract class Setting<T> {
    protected final String name;
    protected T value = null;

    public Setting() {
        name = "";
    }

    public Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public final void setValue(T newValue) {
        value = newValue;
        putInStorage();
        try {
            Settings.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public abstract void putInStorage();
}