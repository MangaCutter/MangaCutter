package net.macu.cutter;

import net.macu.UI.EmptyForm;
import net.macu.UI.Form;
import net.macu.UI.ViewManager;
import net.macu.settings.L;

import java.awt.image.BufferedImage;

public class AsIsCutter implements Cutter {
    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        return fragments;
    }

    @Override
    public String getDescription() {
        return L.get("cutter.AsIsPageForm.description");
    }

    @Override
    public boolean isReturnsSingleFile() {
        return false;
    }

    @Override
    public Form getOptionsForm() {
        return new EmptyForm();
    }

    @Override
    public void cancel() {

    }
}
