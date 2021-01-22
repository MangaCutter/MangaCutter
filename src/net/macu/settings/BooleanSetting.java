package net.macu.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name) {
        super(name);
    }

    @Override
    public String getSerializedValue() {
        return String.valueOf(value);
    }
}
