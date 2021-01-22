package net.macu.settings;

public abstract class Setting<T> {
    private final String name;
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

    public void setValue(T newValue) {
        value = newValue;
    }

    public abstract String getSerializedValue();
}