package net.macu.settings;

public class StringSetting extends Setting<String> {
    public StringSetting(String name) {
        super(name);
    }

    @Override
    public String getSerializedValue() {
        return value;
    }
}
