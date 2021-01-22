package net.macu.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name) {
        super(name);
    }

    @Override
    public void putInStorage() {
        Settings.preferences.putBoolean(name, value);
    }
}
