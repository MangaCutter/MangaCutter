package net.ddns.masterlogick.cutter.pasta;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Frame {
    int fromIndex = -1;
    int fromY = -1;
    int toIndex = -1;
    int toY = -1;
    int height = 0;
    BufferedImage[] fragments;

    public Frame(BufferedImage[] fragments) {
        this.fragments = fragments;
    }

    public Frame(BufferedImage[] fragments, int start, int end) {
        this.fragments = fragments;
        height = end - start + 1;
        fromIndex = toIndex = 0;
        fromY = start;
        toY = end;
        while (toY >= this.fragments[toIndex].getHeight()) {
            toY -= this.fragments[toIndex].getHeight();
            toIndex++;
        }
        while (fromY >= this.fragments[toIndex].getHeight()) {
            fromY -= this.fragments[fromIndex].getHeight();
            fromIndex++;
        }
    }

    public void fixHeight() {
        if (fromIndex == toIndex) {
            height = toY - fromY + 1;
        } else {
            height = fragments[fromIndex].getHeight() - fromY + toY + 1;
            for (int k = fromIndex + 1; k < toIndex; k++) {
                height += fragments[k].getHeight();
            }
        }
    }

    public Frame getTopHalf() {
        int newHeight = height / 2 + height % 2;
        Frame f = new Frame(fragments);
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

    public Frame getBottomHalf() {
        int newHeight = height / 2;
        Frame f = new Frame(fragments);
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

    public BufferedImage createImage() {
        int destWidth = 0;
        for (int i = fromIndex; i <= toIndex; i++) {
            destWidth = Math.max(destWidth, fragments[i].getWidth());
        }
        BufferedImage image = new BufferedImage(destWidth, height, BufferedImage.TYPE_INT_RGB);
        drawOnImage(image, 0, 0, destWidth, height, 1);
        return image;
    }

    public void drawOnImage(BufferedImage dest, int x, int y, int width, int height, float scale) {
        Graphics2D g = dest.createGraphics();
        if (fromIndex == toIndex) {
            g.drawImage(fragments[fromIndex],
                    x, y,
                    x + width, y + height,
                    0, fromY,
                    fragments[fromIndex].getWidth(), toY,
                    null);
        } else {
            g.drawImage(fragments[fromIndex],
                    x, y,
                    x + width, (int) (y + (fragments[fromIndex].getHeight() - fromY) * scale),
                    0, fromY,
                    fragments[fromIndex].getWidth(), fragments[fromIndex].getHeight(),
                    null);
            y += (fragments[fromIndex].getHeight() - fromY) * scale;
            for (int i = fromIndex + 1; i < toIndex; i++) {
                g.drawImage(fragments[i],
                        x, y,
                        x + width, (int) (y + (fragments[i].getHeight()) * scale),
                        0, 0,
                        fragments[i].getWidth(), fragments[i].getHeight(),
                        null);
                y += fragments[i].getHeight() * scale;
            }
            g.drawImage(fragments[toIndex],
                    x, y,
                    x + width, (int) (y + (toY + 1) * scale),
                    0, 0,
                    fragments[toIndex].getWidth(), toY + 1,
                    null);
        }
    }

    @Override
    public String toString() {
        return "[(" + fromIndex + "," + fromY + "),(" + toIndex + "," + toY + ")," + height + "]";
    }
}