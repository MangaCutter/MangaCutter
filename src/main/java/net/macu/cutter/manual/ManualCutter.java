package net.macu.cutter.manual;

import net.macu.UI.EmptyForm;
import net.macu.UI.Form;
import net.macu.UI.ManualCutterFrame;
import net.macu.UI.ViewManager;
import net.macu.cutter.Cutter;
import net.macu.settings.L;

import java.awt.image.BufferedImage;

public class ManualCutter implements Cutter {
    ManualCutterFrame mcf;

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        mcf = new ManualCutterFrame(fragments);
        return mcf.waitForResults();
    }

    @Override
    public String getDescription() {
        return L.get("cutter.ManualForm.description");
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
        if (mcf != null) {
            mcf.cancel();
        }
    }
}
