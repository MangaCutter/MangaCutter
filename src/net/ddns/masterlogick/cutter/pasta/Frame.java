package net.ddns.masterlogick.cutter.pasta;

import java.awt.image.BufferedImage;

class Frame {
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