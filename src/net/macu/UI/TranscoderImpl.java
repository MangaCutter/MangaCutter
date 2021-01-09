package net.macu.UI;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

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

    public BufferedImage getImage() {
        return image;
    }
}