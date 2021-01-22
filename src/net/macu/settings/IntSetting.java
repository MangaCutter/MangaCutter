package net.macu.settings;

public class IntSetting extends Setting<Integer> {
    public IntSetting(String name) {
        super(name);
    }

    @Override
    public void putInStorage() {
        Settings.preferences.putInt(name, value);
    }
}
