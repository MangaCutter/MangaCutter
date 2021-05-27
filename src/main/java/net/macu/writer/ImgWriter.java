package net.macu.writer;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

public interface ImgWriter {
    void writeImage(BufferedImage src, OutputStream dst);

    String getExtension();

    String getDescription();
}
