package net.macu.UI;

import javax.swing.*;
import java.awt.*;

public class IconifiedFileChooser extends JFileChooser {
    private Image icon;

    public IconifiedFileChooser(Image icon) {
        this.setIcon(icon);
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog d = super.createDialog(parent);
        d.setIconImage(getIcon());
        return d;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }
}
