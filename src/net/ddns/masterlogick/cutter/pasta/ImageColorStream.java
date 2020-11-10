package net.ddns.masterlogick.cutter.pasta;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

class ImageColorStream {
    private static final int BUFFER_HEIGHT = 512;
    private static final int TOLERANCE = 20;

    Raster raster;
    int[] buffer;
    int from = -1;
    int to = -1;

    public ImageColorStream(BufferedImage src) {
        raster = src.getData();
    }

    public boolean equalsColorsWithEpsilon(int y, int x, int x1) {
        update(y);
        int rowStartIndex = (y - from) * raster.getWidth();
        return Math.abs(buffer[3 * (rowStartIndex + x)] - buffer[3 * (rowStartIndex + x1)]) <= TOLERANCE &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 1] - buffer[3 * (rowStartIndex + x1) + 1]) <= TOLERANCE &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 2] - buffer[3 * (rowStartIndex + x1) + 2]) <= TOLERANCE;
    }

    public boolean equalsColorsWithEpsilon(int x, int y, int[] color) {
        update(y);
        int rowStartIndex = (y - from) * raster.getWidth();
        return Math.abs(buffer[3 * (rowStartIndex + x)] - color[0]) <= TOLERANCE &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 1] - color[1]) <= TOLERANCE &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 2] - color[2]) <= TOLERANCE;
    }

    public int[] getColor(int x, int y) {
        update(y);
        int[] color = new int[3];
        int rowStartIndex = (y - from) * raster.getWidth();
        color[0] = buffer[3 * (rowStartIndex + x)];
        color[1] = buffer[3 * (rowStartIndex + x) + 1];
        color[2] = buffer[3 * (rowStartIndex + x) + 2];
        return color;
    }

    private void update(int newPos) {
        if (from <= newPos && newPos <= to) return;
        int bufferHeight = Math.max(Math.min(raster.getHeight() - newPos - 1, BUFFER_HEIGHT), 1);
        buffer = raster.getPixels(0, newPos, raster.getWidth(), bufferHeight, (int[]) null);
        from = newPos;
        to = from + bufferHeight - 1;
    }
}
