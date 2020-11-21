package net.macu.cutter.pasta;

import net.macu.UI.ViewManager;
import net.macu.cutter.Cutter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PastaCutter implements Cutter {
    boolean cancel = false;
    int tolerance;
    private Frame current;
    private final int perfectHeight;
    private final boolean saveGradient;
    private static final int MIN_HEIGHT = 30;
    private static final int BORDERS_WIDTH = 10;

    public PastaCutter(int perfectHeight, boolean saveGradient, int tolerance) {
        this.perfectHeight = perfectHeight;
        this.saveGradient = saveGradient;
        this.tolerance = tolerance;
    }

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments) {
        return drawFrames(recognizeFrames(fragments));
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private List<Frame> recognizeFrames(BufferedImage[] fragments) {
        ViewManager.startProgress(fragments.length, "Рассчёт высот сканов: 0/" + fragments.length);
        ArrayList<Frame> frameInfo = new ArrayList<>();
        boolean scanlineOnWhite = true;
        current = new Frame(fragments);
        current.fromY = 0;
        current.fromIndex = 0;
        ImageColorStream ics = new ImageColorStream(fragments[0]);
        for (int x = BORDERS_WIDTH; x < fragments[0].getWidth() - BORDERS_WIDTH; x++) {
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
                for (int x = BORDERS_WIDTH; x < fragments[i].getWidth() - BORDERS_WIDTH; x++) {
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
                        current.toIndex = i - 1;
                        current.toY = fragments[current.toIndex].getHeight() - 1;
                    } else {
                        current.toIndex = i;
                        current.toY = y - 1;
                    }
                    current.fixHeight();
                    frameInfo.add(current);
                    current = new Frame(fragments);
                    current.fromIndex = i;
                    current.fromY = y;
                    scanlineOnWhite = true;
                }
            }
            ViewManager.incrementProgress("Рассчёт высот сканов: " + (i + 1) + "/" + fragments.length);
        }

        if (scanlineOnWhite) {
            if (frameInfo.size() != 0) {
                Frame f = frameInfo.get(frameInfo.size() - 1);
                f.toIndex = fragments.length - 1;
                f.toY = fragments[f.toIndex].getHeight() - 1;
                frameInfo.set(frameInfo.size() - 1, f);
            }
        } else {
            current.toIndex = fragments.length - 1;
            current.toY = fragments[current.toIndex].getHeight() - 1;
            frameInfo.add(frameInfo.size() - 1, current);
        }
        return frameInfo;
    }

    private void newFrameStart(ArrayList<Frame> frameInfo, int i, int y) {
        current.toY = y;
        current.toIndex = i;
        current.fixHeight();
        if (current.height <= MIN_HEIGHT) {
            Frame prev = current;
            if (frameInfo.size() != 0) {
                prev = frameInfo.remove(frameInfo.size() - 1);
            }
            current = prev;
        } else if (frameInfo.size() != 0) {
            Frame f = frameInfo.get(frameInfo.size() - 1);
            Frame frame = current.getTopHalf();
            f.toIndex = frame.toIndex;
            f.toY = frame.toY;
            f.fixHeight();
            frameInfo.set(frameInfo.size() - 1, f);
            current = current.getBottomHalf();
        }
    }

    private BufferedImage[] drawFrames(List<Frame> frames) {
        if (cancel) return null;
        ViewManager.startProgress(frames.size(), "Склейка сканов: 0/" + frames.size());
        ArrayList<BufferedImage> arr = new ArrayList<>();
        int curHeight = 0;
        int prevEnd = -1;
        for (int i = 0; i < frames.size(); i++) {
            if (cancel) return null;
            if (curHeight + frames.get(i).height < perfectHeight && i != frames.size() - 1) {
                curHeight += frames.get(i).height;
                ViewManager.startProgress(frames.size(), "Склейка сканов: " + (i + 1) + "/" + frames.size());
            } else {
                Frame from = frames.get(prevEnd + 1);
                Frame to;
                if (curHeight > 0 && perfectHeight - curHeight <= frames.get(i).height + curHeight - perfectHeight) {
                    i--;
                } else {
                    ViewManager.startProgress(frames.size(), "Склейка сканов: " + (i + 1) + "/" + frames.size());
                }
                to = frames.get(i);
                prevEnd = i;
                curHeight = 0;
                from.toY = to.toY;
                from.toIndex = to.toIndex;
                from.fixHeight();
                arr.add(from.createImage());
            }
        }
        BufferedImage[] buff = new BufferedImage[arr.size()];
        buff = arr.toArray(buff);
        return buff;
    }
}
