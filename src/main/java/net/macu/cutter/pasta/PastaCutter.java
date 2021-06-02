package net.macu.cutter.pasta;

import net.macu.UI.Form;
import net.macu.UI.ViewManager;
import net.macu.UI.cutter.MultiPageForm;
import net.macu.cutter.Cutter;
import net.macu.settings.L;
import net.macu.settings.Settings;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PastaCutter implements Cutter {
    boolean cancel = false;
    int tolerance;
    private Frame current;
    private final MultiPageForm form;
    private int perfectHeight;
    private boolean saveGradient;

    public PastaCutter() {
        form = new MultiPageForm();
    }

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments, ViewManager viewManager) {
        perfectHeight = form.getPerfectHeight();
        saveGradient = form.isSaveGradient();
        tolerance = form.getTolerance();
        return drawFrames(recognizeFrames(fragments, viewManager), viewManager);
    }

    @Override
    public String getDescription() {
        return L.get("cutter.pasta.PastaCutter.description");
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

    private List<Frame> recognizeFrames(BufferedImage[] fragments, ViewManager viewManager) {
        viewManager.startProgress(fragments.length, L.get("cutter.pasta.PastaCutter.recognizeFrames.progress", 0, fragments.length));
        ArrayList<Frame> frameInfo = new ArrayList<>();
        boolean scanlineOnWhite = true;
        current = Frame.create(fragments, 0, 0);
        ImageColorStream ics = new ImageColorStream(fragments[0]);
        for (int x = Settings.PastaCutter_BordersWidth.getValue(); x < fragments[0].getWidth() - Settings.PastaCutter_BordersWidth.getValue(); x++) {
            if (!ics.equalsColorsWithEpsilon(0, fragments[0].getWidth() / 2, x, tolerance)) {
                scanlineOnWhite = false;
            }
        }

        int[] prevColor = ics.getColor(fragments[0].getWidth() / 2, 0);

        for (int i = 0; i < fragments.length; i++) {
            ics = new ImageColorStream(fragments[i]);
            x_label:
            for (int y = 0; y < fragments[i].getHeight(); y++) {
                if (cancel) return null;
                int middle = fragments[i].getWidth() / 2;
                if (scanlineOnWhite && saveGradient && !ics.equalsColorsWithEpsilon(middle, y, prevColor, 0)) {
                    newFrameStart(frameInfo, i, y);
                    scanlineOnWhite = false;
                    continue;
                }
                for (int x = Settings.PastaCutter_BordersWidth.getValue(); x < fragments[i].getWidth() - Settings.PastaCutter_BordersWidth.getValue(); x++) {
                    if (!ics.equalsColorsWithEpsilon(y, middle, x, tolerance)) {
                        if (scanlineOnWhite) {
                            newFrameStart(frameInfo, i, y);
                            scanlineOnWhite = false;
                        }
                        continue x_label;
                    }
                }

                if (!scanlineOnWhite && saveGradient && !ics.equalsColorsWithEpsilon(middle, y, prevColor, 0)) {
                    prevColor = ics.getColor(middle, y);
                    continue;
                }

                prevColor = ics.getColor(middle, y);

                if (i == 0 && y == 0) {//reached only if scanlineOnWhite == true. see first loop in this method
                    continue;
                }
                if (!scanlineOnWhite) {
                    if (y == 0) {
                        current.extendToEndOfFragment(i - 1);
                    } else {
                        current.setEnd(i, y - 1);
                    }
                    frameInfo.add(current);
                    current = Frame.create(fragments, i, y);
                    scanlineOnWhite = true;
                }
            }
            viewManager.incrementProgress(L.get("cutter.pasta.PastaCutter.recognizeFrames.progress", (i + 1), fragments.length));
        }

        if (scanlineOnWhite) {
            if (frameInfo.size() != 0) {
                Frame f = frameInfo.get(frameInfo.size() - 1);
                f.extendToEnd();
                frameInfo.set(frameInfo.size() - 1, f);
            }
        } else {
            current.extendToEnd();
            frameInfo.add(current);
        }
        return frameInfo;
    }

    private void newFrameStart(ArrayList<Frame> frameInfo, int i, int y) {
        current.setEnd(i, y);
        if (current.getHeight() <= Settings.PastaCutter_MinHeight.getValue()) {
            Frame prev = current;
            if (frameInfo.size() != 0) {
                prev = frameInfo.remove(frameInfo.size() - 1);
            }
            current = prev;
        } else if (frameInfo.size() != 0) {
            Frame f = frameInfo.get(frameInfo.size() - 1);
            Frame frame = current.getTopHalf();
            f.copyEndFrom(frame);
            frameInfo.set(frameInfo.size() - 1, f);
            current = current.getBottomHalf();
        }
    }

    private BufferedImage[] drawFrames(List<Frame> frames, ViewManager viewManager) {
        if (cancel) return null;
        viewManager.startProgress(frames.size(), L.get("cutter.pasta.PastaCutter.drawFrames.progress", 0, frames.size()));
        ArrayList<BufferedImage> arr = new ArrayList<>();
        int curHeight = 0;
        int prevEnd = -1;
        for (int i = 0; i < frames.size(); i++) {
            if (cancel) return null;
            if (curHeight + frames.get(i).getHeight() < perfectHeight && i != frames.size() - 1) {
                curHeight += frames.get(i).getHeight();
                viewManager.incrementProgress(L.get("cutter.pasta.PastaCutter.drawFrames.progress", (i + 1), frames.size()));
            } else {
                Frame from = frames.get(prevEnd + 1);
                Frame to;
                if (curHeight > 0 && perfectHeight - curHeight <= frames.get(i).getHeight() + curHeight - perfectHeight) {
                    i--;
                } else {
                    viewManager.incrementProgress(L.get("cutter.pasta.PastaCutter.drawFrames.progress", (i + 1), frames.size()));
                }
                to = frames.get(i);
                prevEnd = i;
                curHeight = 0;
                from.copyEndFrom(to);
                arr.add(from.createImage());
            }
        }
        BufferedImage[] buff = new BufferedImage[arr.size()];
        buff = arr.toArray(buff);
        return buff;
    }
}
