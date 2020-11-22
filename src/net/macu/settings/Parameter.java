package net.macu.settings;

public class Parameter {
    Type type;
    String name;
    Object value;

    public Parameter(Parameter.Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public void setValue(Object newValue) {
        value = newValue;
        switch (type) {
            case INT_TYPE:
                Settings.preferences.putInt(name, (Integer) value);
                break;
            case BOOLEAN_TYPE:
                Settings.preferences.putBoolean(name, (Boolean) value);
                break;
            case STRING_TYPE:
                Settings.preferences.put(name, (String) value);
                break;
        }
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Integer getInt() {
        return (type == Type.INT_TYPE) ? (Integer) value : null;
    }

    public String getString() {
        return (type == Type.STRING_TYPE) ? (String) value : null;
    }

    public Boolean getBoolean() {
        return (type == Type.BOOLEAN_TYPE) ? (Boolean) value : null;
    }

    public enum Type {
        INT_TYPE, STRING_TYPE, BOOLEAN_TYPE
    }
}