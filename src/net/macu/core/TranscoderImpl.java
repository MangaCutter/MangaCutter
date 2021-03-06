package net.macu.core;

import net.macu.UI.IconManager;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class TranscoderImpl extends ImageTranscoder {
    private BufferedImage image = null;

    public BufferedImage createImage(int w, int h) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return image;
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput output) {
    }

    public BufferedImage transcodeImage(String filename) {
        try {
            transcode(new TranscoderInput(IconManager.class.getResourceAsStream(filename)), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        return image;
    }

    public ImageIcon transcodeIcon(String filename) {
        return new ImageIcon(transcodeImage(filename));
    }
}