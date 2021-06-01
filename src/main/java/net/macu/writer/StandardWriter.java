package net.macu.writer;

import net.macu.settings.L;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class StandardWriter {
    public static ImgWriter createImageIOWriter(String ext) {
        return new ImgWriter() {
            @Override
            public void writeImage(BufferedImage src, OutputStream dst) {
                try {
                    ImageIO.write(src, ext.toLowerCase(), dst);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String getExtension() {
                return ext.toLowerCase();
            }

            @Override
            public String getDescription() {
                return L.get("imgWriter.StandardWriter.description", ext.toUpperCase());
            }

            @Override
            public Component getOptionsPanel() {
                return new JPanel();
            }
        };
    }
}
