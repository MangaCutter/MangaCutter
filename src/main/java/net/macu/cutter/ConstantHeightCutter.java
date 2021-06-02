package net.macu.cutter;

import net.macu.UI.Form;
import net.macu.UI.ViewManager;
import net.macu.UI.cutter.ConstantHeightForm;
import net.macu.cutter.pasta.Frame;
import net.macu.settings.L;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ConstantHeightCutter implements Cutter {
    private boolean cancel = false;
    private final ConstantHeightForm form;

    public ConstantHeightCutter() {
        form = new ConstantHeightForm();
    }

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        int height = form.getImgHeight();
        viewManager.startProgress(1, L.get("cutter.ConstantHeightCutter.cutScans.progress"));
        ArrayList<Frame> result = new ArrayList<>();
        for (int currentOffset = 0; ; currentOffset += height) {
            if (cancel) return null;
            Frame f = new Frame(fragments, currentOffset, currentOffset + height - 1);
            if (f.getHeight() > 0) result.add(f);
            if (f.getHeight() != height) break;
        }
        return result.stream().map(Frame::createImage).toArray(BufferedImage[]::new);
    }

    @Override
    public String getDescription() {
        return L.get("cutter.ConstantHeightForm.description");
    }

    @Override
    public boolean isReturnsSingleFile() {
        return false;
    }

    @Override
    public Form getOptionsForm() {
        return form;
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
