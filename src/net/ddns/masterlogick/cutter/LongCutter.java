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

        for (int i = 0; i < fragments.length; i++) {
            int[] data = fragments[i].getRaster().getPixels(0, 0, fragments[i].getWidth(), fragments[i].getHeight(), (int[]) null);
            x:
            for (int j = 0; j < fragments[i].getHeight(); j++) {
                for (int k = 0; k < fragments[i].getWidth(); k++) {
                    int left = j * fragments[i].getWidth();
                    int right = j * fragments[i].getWidth() + k;
                    if (data[3 * left] != data[3 * right] || data[3 * left + 1] != data[3 * right + 1] || data[3 * left + 2] != data[3 * right + 2]) {
                        if (scanlineOnWhite) {
                            current = nextFrame(fragments, frameInfo, current, i, j);
                            scanlineOnWhite = false;
                        }
                        continue x;
                    }
                }
                if (j != fragments[i].getHeight() - 1 && !scanlineOnWhite) {
                    current = nextFrame(fragments, frameInfo, current, i, j);
                    scanlineOnWhite = true;
                }
            }
            ViewManager.incrementProgress("Рассчёт высот сканов: " + (i + 1) + "/" + fragments.length);
        }

        nextFrame(fragments, frameInfo, current, fragments.length - 1, fragments[fragments.length - 1].getHeight() - 1);
        if (frameInfo.size() % 2 == 0) {
            Frame f = new Frame();
            f.height = 0;
            f.toIndex = f.fromY = fragments.length - 1;
            f.toY = f.fromY = fragments[f.toY].getHeight() - 1;
            frameInfo.add(f);
        }

        ArrayList<BufferedImage> arr = new ArrayList<>();
        int curHeight = 0;
        int prevHeight = 0;
        int fromIndex = 1;
        for (int i = 1; i < frameInfo.size(); i += 2) {
            if (curHeight + frameInfo.get(i - 1).height / 2 + frameInfo.get(i).height + frameInfo.get(i + 1).height / 2 > MAX_HEIGHT) {
                BufferedImage bf = new BufferedImage(fragments[0].getWidth(), curHeight, BufferedImage.TYPE_INT_RGB);
                Graphics g = bf.getGraphics();
                Frame first = getSecondHalf(fragments, frameInfo.get(fromIndex - 1));
                Frame second = getFirstHalf(fragments, frameInfo.get(i - 1));
                second.toY = first.toY;
                second.toIndex = first.toIndex;
                drawFrame(g, 0, fragments, second);
                fromIndex = i + 1;
                prevHeight = curHeight;
                curHeight = 0;
            } else {
                prevHeight = curHeight;
                curHeight += frameInfo.get(i - 1).height / 2 + frameInfo.get(i).height + frameInfo.get(i + 1).height / 2;
            }
        }
        return null;
    }

    private void drawFrame(Graphics g, int y, BufferedImage[] fragments, Frame f) {
        if (f.fromIndex == f.toIndex) {
            g.drawImage(fragments[f.fromIndex],
                    0, y,
                    fragments[f.fromIndex].getWidth() - 1, y + f.height - 1,
                    0, f.fromY,
                    fragments[f.fromIndex].getWidth() - 1, f.toY,
                    null);
        } else {
            g.drawImage(fragments[f.fromIndex],
                    0, y,
                    fragments[f.fromIndex].getWidth() - 1, y + fragments[f.fromIndex].getWidth() - f.fromY - 1,
                    0, f.fromY,
                    0, fragments[f.fromIndex].getWidth() - 1,
                    null);
            y += fragments[f.fromIndex].getWidth() - f.fromY;
            for (int i = f.fromIndex + 1; i < f.toIndex; i++) {
                g.drawImage(fragments[i],
                        0, y,
                        fragments[i].getWidth() - 1, y + fragments[i].getHeight() - 1,
                        0, 0,
                        fragments[i].getWidth() - 1, fragments[i].getHeight() - 1,
                        null);
                y += fragments[i].getHeight();
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
        f.fromIndex = f.toY;
        f.fromY = f.toY - newHeight + 1;
        while (f.fromY < 0) {
            f.fromIndex--;
            f.fromY += fragments[f.fromIndex].getHeight();
        }
        return f;
    }

    private Frame nextFrame(BufferedImage[] fragments, ArrayList<Frame> frameInfo, Frame current, int i, int j) {
        current.toIndex = i;
        current.toY = j - 1;
        if (current.fromIndex == current.toIndex) {
            current.height = current.toY - current.fromY + 1;
        } else {
            current.height = fragments[current.fromIndex].getHeight() - current.fromY + current.toY + 1;
            current.height = fragments[current.fromIndex].getHeight() - current.fromY + current.toY + 1;
            for (int k = current.fromIndex + 1; k < current.toIndex; k++) {
                current.height += fragments[k].getHeight();
            }
        }
        frameInfo.add(current);
        current = new Frame();
        current.fromIndex = i;
        current.fromY = j;
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
