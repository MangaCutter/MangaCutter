package net.ddns.masterlogick.cutter;

import net.ddns.masterlogick.UI.ViewManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class LongCutter implements Cutter {
    boolean cancel = false;
    private static final int SKIP_THROTTLE = 15;
    private static final int MAX_HEIGHT = 10000;

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments) {
        ViewManager.startProgress(fragments.length, "Рассчёт высот сканов: 0/" + fragments.length);
        ArrayList<Frame> frameInfo = new ArrayList<>();
        boolean scanlineOnWhite = true;
        Frame current = new Frame();
        current.fromY = 0;
        current.fromIndex = 0;
        int[] data = fragments[0].getRaster().getPixels(0, 0, fragments[0].getWidth(), fragments[0].getHeight(), (int[]) null);
        for (int k = 0; k < fragments[0].getWidth(); k++) {
            if (data[0] != data[3 * k] || data[1] != data[3 * k + 1] || data[2] != data[3 * k + 2]) {
                scanlineOnWhite = false;
            }
        }
        for (int i = 0; i < fragments.length; i++) {
            data = fragments[i].getRaster().getPixels(0, 0, fragments[i].getWidth(), fragments[i].getHeight(), (int[]) null);
            x:
            for (int j = 0; j < fragments[i].getHeight(); j++) {
                for (int k = 0; k < fragments[i].getWidth(); k++) {
                    int left = j * fragments[i].getWidth();
                    int right = j * fragments[i].getWidth() + k;
                    if (data[3 * left] != data[3 * right] || data[3 * left + 1] != data[3 * right + 1] ||
                            data[3 * left + 2] != data[3 * right + 2]) {
                        if (scanlineOnWhite) {
                            current.toY = j;
                            current.toIndex = i;
                            current = fixHeight(fragments, current);
                            if (frameInfo.size() != 0) {
                                Frame f = frameInfo.get(frameInfo.size() - 1);
                                Frame frame = getFirstHalf(fragments, current);
                                f.toIndex = frame.toIndex;
                                f.toY = frame.toY;
                                f = fixHeight(fragments, f);
                                frameInfo.set(frameInfo.size() - 1, f);
                                current = getSecondHalf(fragments, current);
                            }
                            scanlineOnWhite = false;
                        }
                        continue x;
                    }
                }
                if (i == 0 && j == 0) {//reached only if scanlineOnWhite == true. see first loop in this method
                    continue;
                }
                if (!scanlineOnWhite) {
                    if (j == 0) {
                        current.toIndex = i - 1;
                        current.toY = fragments[current.toIndex].getHeight() - 1;
                    } else {
                        current.toIndex = i;
                        current.toY = j - 1;
                    }
                    current = fixHeight(fragments, current);
                    frameInfo.add(current);
                    current = new Frame();
                    current.fromIndex = i;
                    current.fromY = j;
                    scanlineOnWhite = true;
                }
            }
            ViewManager.incrementProgress("Рассчёт высот сканов: " + (i + 1) + "/" + fragments.length);
        }

        if (scanlineOnWhite) {
            if (frameInfo.size() != 0) {
                Frame f = frameInfo.get(frameInfo.size() - 1);
                f.toIndex = current.toIndex;
                f.toY = current.toY;
                frameInfo.set(frameInfo.size() - 1, f);
            }
        } else {
            current.toIndex = fragments.length - 1;
            current.toY = fragments[current.toIndex].getHeight() - 1;
            frameInfo.add(frameInfo.size() - 1, current);
        }

        ViewManager.startProgress(frameInfo.size(), "Склейка сканов: 0/" + frameInfo.size());
        ArrayList<BufferedImage> arr = new ArrayList<>();
        int curHeight = 0;
        int prevEnd = -1;
        for (int i = 0; i < frameInfo.size(); i++) {
            if (curHeight + frameInfo.get(i).height < MAX_HEIGHT && i != frameInfo.size() - 1) {
                curHeight += frameInfo.get(i).height;
            } else {
                Frame from = frameInfo.get(prevEnd + 1);
                Frame to;
                if (curHeight > 0 && MAX_HEIGHT - curHeight <= frameInfo.get(i).height + curHeight - MAX_HEIGHT) {
                    to = frameInfo.get(i - 1);
                    prevEnd = i - 1;
                } else {
                    to = frameInfo.get(i);
                    prevEnd = i;
                }
                from.toY = to.toY;
                from.toIndex = to.toIndex;
                from = fixHeight(fragments, from);
                BufferedImage bf = new BufferedImage(fragments[from.fromIndex].getWidth(), from.height, BufferedImage.TYPE_INT_RGB);
                Graphics g = bf.getGraphics();
                drawFrame(g, fragments, from);
                arr.add(bf);
            }
        }
        BufferedImage[] buff = new BufferedImage[arr.size()];
        buff = arr.toArray(buff);
        return buff;
    }

    private void drawFrame(Graphics g, BufferedImage[] fragments, Frame f) {
        if (f.fromIndex == f.toIndex) {
            g.drawImage(fragments[f.fromIndex],
                    0, 0,
                    fragments[f.fromIndex].getWidth() - 1, f.height - 1,
                    0, f.fromY,
                    fragments[f.fromIndex].getWidth() - 1, f.toY,
                    null);
        } else {
            int y = 0;
            g.drawImage(fragments[f.fromIndex],
                    0, 0,
                    fragments[f.fromIndex].getWidth() - 1, fragments[f.fromIndex].getHeight() - f.fromY - 1,
                    0, f.fromY,
                    fragments[f.fromIndex].getWidth() - 1, fragments[f.fromIndex].getHeight() - 1,
                    null);
            y += fragments[f.fromIndex].getHeight() - f.fromY-1;
            for (int i = f.fromIndex + 1; i < f.toIndex; i++) {
                g.drawImage(fragments[i],
                        0, y,
                        fragments[i].getWidth() - 1, y + fragments[i].getHeight() - 1,
                        0, 0,
                        fragments[i].getWidth() - 1, fragments[i].getHeight() - 1,
                        null);
                y += fragments[i].getHeight()-1;
            }
            g.drawImage(fragments[f.toIndex],
                    0, y,
                    fragments[f.toIndex].getWidth() - 1, y + f.toY,
                    0, 0,
                    fragments[f.toIndex].getWidth() - 1, f.toY,
                    null);
        }
    }

    private Frame getFirstHalf(BufferedImage[] fragments, Frame frame) {
        int newHeight = frame.height / 2 + frame.height % 2;
        Frame f = new Frame();
        f.fromIndex = frame.fromIndex;
        f.fromY = frame.fromY;
        f.toIndex = f.fromIndex;
        f.toY = f.fromY + newHeight - 1;
        f.height = newHeight;
        while (f.toY >= fragments[f.toIndex].getHeight()) {
            f.toY -= fragments[f.toIndex].getHeight();
            f.toIndex++;
        }
        return f;
    }

    private Frame getSecondHalf(BufferedImage[] fragments, Frame frame) {
        int newHeight = frame.height / 2;
        Frame f = new Frame();
        f.toIndex = frame.toIndex;
        f.toY = frame.toY;
        f.height = newHeight;
        f.fromIndex = f.toIndex;
        f.fromY = f.toY - newHeight + 1;
        while (f.fromY < 0) {
            f.fromIndex--;
            f.fromY += fragments[f.fromIndex].getHeight();
        }
        return f;
    }

    private Frame fixHeight(BufferedImage[] fragments, Frame current) {
        if (current.fromIndex == current.toIndex) {
            current.height = current.toY - current.fromY + 1;
        } else {
            current.height = fragments[current.fromIndex].getHeight() - current.fromY + current.toY + 1;
            for (int k = current.fromIndex + 1; k < current.toIndex; k++) {
                current.height += fragments[k].getHeight();
            }
        }
        return current;
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private class Frame {
        int fromIndex = -1;
        int fromY = -1;
        int toIndex = -1;
        int toY = -1;
        int height = 0;
    }
}
