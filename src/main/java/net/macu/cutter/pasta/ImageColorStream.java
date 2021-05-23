package net.macu.cutter.pasta;

import net.macu.settings.Settings;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class ImageColorStream {

    Raster raster;
    int[] buffer;
    int from = -1;
    int to = -1;

    public ImageColorStream(BufferedImage src) {
        raster = src.getData();
    }

    public boolean equalsColorsWithEpsilon(int y, int x, int x1, int tolerance) {
        update(y);
        int rowStartIndex = (y - from) * raster.getWidth();
        return Math.abs(buffer[3 * (rowStartIndex + x)] - buffer[3 * (rowStartIndex + x1)]) <= tolerance &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 1] - buffer[3 * (rowStartIndex + x1) + 1]) <= tolerance &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 2] - buffer[3 * (rowStartIndex + x1) + 2]) <= tolerance;
    }

    public boolean equalsColorsWithEpsilon(int x, int y, int[] color, int tolerance) {
        update(y);
        int rowStartIndex = (y - from) * raster.getWidth();
        return Math.abs(buffer[3 * (rowStartIndex + x)] - color[0]) <= tolerance &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 1] - color[1]) <= tolerance &&
                Math.abs(buffer[3 * (rowStartIndex + x) + 2] - color[2]) <= tolerance;
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
        int bufferHeight = Math.max(Math.min(raster.getHeight() - newPos - 1, Settings.ImageColorStream_BufferHeight.getValue()), 1);
        buffer = raster.getPixels(0, newPos, raster.getWidth(), bufferHeight, (int[]) null);
        from = newPos;
        to = from + bufferHeight - 1;
    }
}
