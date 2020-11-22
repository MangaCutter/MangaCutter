package net.macu.settings;

import java.util.ArrayList;

public class Parameters extends ArrayList<Parameter> {
    private final String name;

    public Parameters(String name, Parameter... params) {
        super();
        this.name = name;
        for (Parameter param : params) {
            add(param);
        }
    }

    public String getName() {
        return name;
    }
}
