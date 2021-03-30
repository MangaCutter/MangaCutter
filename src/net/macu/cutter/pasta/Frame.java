package net.macu.cutter.pasta;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Frame {
    private final BufferedImage[] fragments;
    private int fromIndex = -1;
    private int fromY = -1;
    private int toIndex = -1;
    private int toY = -1;
    private int height = 0;

    private Frame(BufferedImage[] fragments) {
        this.fragments = fragments;
    }

    public static Frame create(BufferedImage[] fragments, int firstFragment, int YOffset) {
        Frame f = new Frame(fragments);
        f.fromIndex = firstFragment;
        f.fromY = YOffset;
        return f;
    }

    public Frame(BufferedImage[] fragments, int start, int end) {
        this.fragments = fragments;
        fromIndex = toIndex = 0;
        fromY = start;
        toY = end;
        while (toY >= this.fragments[toIndex].getHeight()) {
            toY -= this.fragments[toIndex].getHeight();
            toIndex++;
            if (toIndex == this.fragments.length - 1 && toY >= this.fragments[toIndex].getHeight()) {
                toY = this.fragments[toIndex].getHeight();
                break;
            }
        }
        while (fromY >= this.fragments[fromIndex].getHeight()) {
            fromY -= this.fragments[fromIndex].getHeight();
            fromIndex++;
            if (fromIndex == this.fragments.length - 1 && fromY >= this.fragments[fromIndex].getHeight()) {
                fromY = this.fragments[fromIndex].getHeight();
                break;
            }
        }
        fixHeight();
    }

    private void fixHeight() {
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

    /*private void markAsBorder(Point p, BufferedImage image) {
        image.setRGB(p.x, p.y - fromY, Color.BLACK.getRGB());
    }

    private void markAsBabble(Point p, BufferedImage image) {
        image.setRGB(p.x, p.y - fromY, Color.WHITE.getRGB());
    }

    private boolean isLeftABorder(Point p) {
        if (p.x != 0) {
            if()
        } else return true;
    }

    public void fillArea(Point begin, Color c) {
        BufferedImage img = new BufferedImage(fragments[0].getWidth(), height, BufferedImage.TYPE_BYTE_GRAY);
        ArrayList<Point> searchList = new ArrayList<>();
        searchList.add(begin);
        markAsBabble(begin, img);
        while (!searchList.isEmpty()) {

        }
    }*/

    @Override
    public String toString() {
        return "[(" + fromIndex + "," + fromY + "),(" + toIndex + "," + toY + ")," + height + "]";
    }

    public void extendToEnd() {
        toIndex = fragments.length - 1;
        toY = fragments[toIndex].getHeight() - 1;
        fixHeight();
    }

    public int getHeight() {
        return height;
    }

    public void extendToEndOfFragment(int i) {
        toIndex = i;
        toY = fragments[i].getHeight() - 1;
        fixHeight();
    }

    public void copyEndFrom(Frame frame) {
        toIndex = frame.toIndex;
        toY = frame.toY;
        fixHeight();
    }

    public void setEnd(int lastFragment, int YOffset) {
        toIndex = lastFragment;
        toY = YOffset;
        fixHeight();
    }
}