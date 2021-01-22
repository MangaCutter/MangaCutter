package net.macu.settings;

public class IntSetting extends Setting<Integer> {
    public IntSetting(String name) {
        super(name);
    }

    @Override
    public String getSerializedValue() {
        return String.valueOf(value);
    }
}
