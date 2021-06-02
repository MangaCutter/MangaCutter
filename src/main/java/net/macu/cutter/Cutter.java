package net.macu.cutter;

import net.macu.UI.Form;
import net.macu.UI.ViewManager;

import java.awt.image.BufferedImage;

public interface Cutter {

    BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager);

    String getDescription();

    boolean isReturnsSingleFile();

    Form getOptionsForm();

    void cancel();
}
