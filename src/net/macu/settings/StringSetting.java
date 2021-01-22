package net.macu.settings;

public class StringSetting extends Setting<String> {
    public StringSetting(String name) {
        super(name);
    }

    @Override
    public void putInStorage() {
        Settings.preferences.put(name, value);
    }
}
