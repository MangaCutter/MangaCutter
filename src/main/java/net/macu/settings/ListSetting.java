package net.macu.settings;

import javax.swing.*;
import java.awt.*;

public class ListSetting extends Setting<String> {
    private final String[] variants;
    private final boolean localize;

    public ListSetting(String name, String[] variants, boolean localize) {
        super(name);
        this.variants = variants;
        this.localize = localize;
    }

    public String[] getVariants() {
        return variants;
    }

    @Override
    public void putInStorage() {
        Settings.preferences.put(name, value);
    }

    public javax.swing.ListCellRenderer getRenderer() {
        if (localize) return new ListCellRenderer();
        else return new DefaultListCellRenderer();
    }

    private class ListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, L.get(name + "." + value), index, isSelected, cellHasFocus);
        }
    }
}
