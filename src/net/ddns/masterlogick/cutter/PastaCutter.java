package net.ddns.masterlogick.cutter;

import net.ddns.masterlogick.UI.ViewManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PastaCutter implements Cutter {
    boolean cancel = false;
    private final int perfectHeight;
    private final boolean cutOnGradient;
    private static final int COLOR_THRESHOLD = 5;
    private static final int MIN_HEIGHT = 30;

    public PastaCutter(int perfectHeight, boolean cutOnGradient) {
        this.perfectHeight = perfectHeight;
        this.cutOnGradient = cutOnGradient;
    }

    @Override
    public BufferedImage[] cutScans(BufferedImage[] fragments) {
        return drawFrames(fragments, recognizeFrames(fragments));
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private List<Frame> recognizeFrames(BufferedImage[] fragments) {
        ViewManager.startProgress(fragments.length, "Рассчёт высот сканов: 0/" + fragments.length);
        ArrayList<Frame> frameInfo = new ArrayList<>();
        boolean scanlineOnWhite = true;
        Frame current = new Frame();
        current.fromY = 0;
        current.fromIndex = 0;
        int[] data = fragments[0].getRaster().getPixels(0, 0, fragments[0].getWidth(), fragments[0].getHeight(), (int[]) null);
        for (int k = 0; k < fragments[0].getWidth(); k++) {
            if (!equalsWithEpsilon(data, 0, data, k)) {
                scanlineOnWhite = false;
            }
        }

        int[] prevColor = new int[3];
        prevColor[0] = data[0];
        prevColor[1] = data[1];
        prevColor[2] = data[2];

        for (int i = 0; i < fragments.length; i++) {
            data = fragments[i].getRaster().getPixels(0, 0, fragments[i].getWidth(), fragments[i].getHeight(), (int[]) null);
            x:
            for (int j = 0; j < fragments[i].getHeight(); j++) {
                if (cancel) return null;
                int left = j * fragments[i].getWidth();
                if (scanlineOnWhite && !cutOnGradient && !equalsWithEpsilon(data, left, prevColor, 0)) {
                    current.toY = j;
                    current.toIndex = i;
                    current.fixHeight(fragments);
                    if (current.height <= MIN_HEIGHT) {
                        Frame prev = current;
                        if (frameInfo.size() != 0) {
                            prev = frameInfo.remove(frameInfo.size() - 1);
                        }
                        current = prev;
                    } else if (frameInfo.size() != 0) {
                        Frame f = frameInfo.get(frameInfo.size() - 1);
                        Frame frame = current.getFirstHalf(fragments);
                        f.toIndex = frame.toIndex;
                        f.toY = frame.toY;
                        f.fixHeight(fragments);
                        frameInfo.set(frameInfo.size() - 1, f);
                        current = current.getSecondHalf(fragments);
                    }
                    scanlineOnWhite = false;
                    continue;
                }
                for (int k = 0; k < fragments[i].getWidth(); k++) {
                    int right = j * fragments[i].getWidth() + k;
                    if (!equalsWithEpsilon(data, left, data, right)) {
                        if (scanlineOnWhite) {
                            current.toY = j;
                            current.toIndex = i;
                            current.fixHeight(fragments);
                            if (current.height <= MIN_HEIGHT) {
                                Frame prev = current;
                                if (frameInfo.size() != 0) {
                                    prev = frameInfo.remove(frameInfo.size() - 1);
                                }
                                current = prev;
                            } else if (frameInfo.size() != 0) {
                                Frame f = frameInfo.get(frameInfo.size() - 1);
                                Frame frame = current.getFirstHalf(fragments);
                                f.toIndex = frame.toIndex;
                                f.toY = frame.toY;
                                f.fixHeight(fragments);
                                frameInfo.set(frameInfo.size() - 1, f);
                                current = current.getSecondHalf(fragments);
                            }
                            scanlineOnWhite = false;
                        }
                        continue x;
                    }
                }

                if (!scanlineOnWhite && !cutOnGradient && !equalsWithEpsilon(data, left, prevColor, 0)) {
                    prevColor[0] = data[3 * left];
                    prevColor[1] = data[3 * left + 1];
                    prevColor[2] = data[3 * left + 2];
                    continue;
                }

                prevColor[0] = data[3 * left];
                prevColor[1] = data[3 * left + 1];
                prevColor[2] = data[3 * left + 2];

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
                    current.fixHeight(fragments);
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

    private BufferedImage[] drawFrames(BufferedImage[] fragments, List<Frame> frames) {
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
                    to = frames.get(i - 1);
                    prevEnd = i - 1;
                } else {
                    ViewManager.startProgress(frames.size(), "Склейка сканов: " + (i + 1) + "/" + frames.size());
                    to = frames.get(i);
                    prevEnd = i;
                }
                curHeight = 0;
                from.toY = to.toY;
                from.toIndex = to.toIndex;
                from.fixHeight(fragments);
                arr.add(copyImageFromFrame(fragments, from));
            }
        }
        BufferedImage[] buff = new BufferedImage[arr.size()];
        buff = arr.toArray(buff);
        return buff;
    }

    private BufferedImage copyImageFromFrame(BufferedImage[] fragments, Frame frame) {
        int destWidth = 0;
        for (int i = frame.fromIndex; i <= frame.toIndex; i++) {
            destWidth = Math.max(destWidth, fragments[i].getWidth());
        }
        BufferedImage image = new BufferedImage(destWidth, frame.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        if (frame.fromIndex == frame.toIndex) {
            g.drawImage(fragments[frame.fromIndex],
                    0, 0,
                    destWidth, frame.height - 1,
                    0, frame.fromY,
                    fragments[frame.fromIndex].getWidth(), frame.toY,
                    null);
        } else {
            int y = 0;
            g.drawImage(fragments[frame.fromIndex],
                    0, 0,
                    destWidth, fragments[frame.fromIndex].getHeight() - frame.fromY,
                    0, frame.fromY,
                    fragments[frame.fromIndex].getWidth(), fragments[frame.fromIndex].getHeight(),
                    null);
            y += fragments[frame.fromIndex].getHeight() - frame.fromY;
            for (int i = frame.fromIndex + 1; i < frame.toIndex; i++) {
                if (cancel) return null;
                g.drawImage(fragments[i],
                        0, y,
                        destWidth, y + fragments[i].getHeight(),
                        0, 0,
                        fragments[i].getWidth(), fragments[i].getHeight(),
                        null);
                y += fragments[i].getHeight();
            }
            g.drawImage(fragments[frame.toIndex],
                    0, y,
                    destWidth, y + frame.toY + 1,
                    0, 0,
                    fragments[frame.toIndex].getWidth(), frame.toY + 1,
                    null);
        }
        return image;
    }

    private static boolean equalsWithEpsilon(int[] color1, int i1, int[] color2, int i2) {
        return Math.abs(color1[3 * i1] - color2[3 * i2]) <= COLOR_THRESHOLD &&
                Math.abs(color1[3 * i1 + 1] - color2[3 * i2 + 1]) <= COLOR_THRESHOLD &&
                Math.abs(color1[3 * i1 + 2] - color2[3 * i2 + 2]) <= COLOR_THRESHOLD;
    }

    private static class Frame {
        int fromIndex = -1;
        int fromY = -1;
        int toIndex = -1;
        int toY = -1;
        int height = 0;

        void fixHeight(BufferedImage[] fragments) {
            if (fromIndex == toIndex) {
                height = toY - fromY + 1;
            } else {
                height = fragments[fromIndex].getHeight() - fromY + toY + 1;
                for (int k = fromIndex + 1; k < toIndex; k++) {
                    height += fragments[k].getHeight();
                }
            }
        }

        Frame getFirstHalf(BufferedImage[] fragments) {
            int newHeight = height / 2 + height % 2;
            Frame f = new Frame();
            f.fromIndex = fromIndex;
            f.fromY = fromY;
            f.toIndex = f.fromIndex;
            f.toY = f.fromY + newHeight - 1;
            f.height = newHeight;
            while (f.toY >= fragments[f.toIndex].getHeight()) {
                f.toY -= fragments[f.toIndex].getHeight();
                f.toIndex++;
            }
            return f;
        }

        Frame getSecondHalf(BufferedImage[] fragments) {
            int newHeight = height / 2;
            Frame f = new Frame();
            f.toIndex = toIndex;
            f.toY = toY;
            f.height = newHeight;
            f.fromIndex = f.toIndex;
            f.fromY = f.toY - newHeight + 1;
            while (f.fromY < 0) {
                f.fromIndex--;
                f.fromY += fragments[f.fromIndex].getHeight();
            }
            return f;
        }

        @Override
        public String toString() {
            return "[(" + fromIndex + "," + fromY + "),(" + toIndex + "," + toY + ")," + height + "]";
        }
    }
}
