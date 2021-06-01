package net.macu.writer;

import net.macu.UI.imgWriter.PsdForm;
import net.macu.settings.L;
import net.macu.writer.psd.UnsupportedColorSpaceTypeException;
import psd.psdwiter.ImageDataWriter;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class PsbWriter implements ImgWriter {
    private final PsdForm options;

    public PsbWriter() {
        options = new PsdForm();
    }

    @Override
    public void writeImage(BufferedImage src, OutputStream dst) {
        ColorModel model = src.getColorModel();
        //---File header---
        ByteBuffer header = ByteBuffer.allocate(34);
        header.order(ByteOrder.BIG_ENDIAN);
        //Signature: always equal to '8BPS'
        header.put(new byte[]{0x38, 0x42, 0x50, 0x53});
        //**PSB** version is 2
        header.putShort((short) 2);
        //Reserved: must be zero
        header.put(new byte[6]);
        //The number of channels in the image, including any alpha channels
        header.putShort((short) model.getComponentSize().length);
        //The height of the image in pixels
        header.putInt(src.getHeight());
        //The width of the image in pixels
        header.putInt(src.getWidth());
        //Depth: the number of bits per channel
        header.putShort((short) Arrays.stream(model.getComponentSize()).max().getAsInt());
        //The color mode of the file
        int colorModeType = model.getColorSpace().getType();
        if (colorModeType == ColorSpace.TYPE_GRAY)
            header.putShort((short) 0x1);
        else if (colorModeType == ColorSpace.TYPE_RGB) {
            header.putShort((short) 0x3);
        } else if (colorModeType == ColorSpace.TYPE_CMYK) {
            header.putShort((short) 0x4);
        } else if (colorModeType == ColorSpace.TYPE_Lab) {
            header.putShort((short) 0x9);
        } else try {
            throw new UnsupportedColorSpaceTypeException(colorModeType);
        } catch (UnsupportedColorSpaceTypeException e) {
            e.printStackTrace();
        }
        //---Color mode data---
        //---Image resources---
        header.put(new byte[8]);
        try {
            dst.write(header.array());
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*//---Layer and mask information---
        ByteBuffer layerInfo = ByteBuffer.allocate();
        layerInfo.order(ByteOrder.BIG_ENDIAN);
        //Length of the layer and mask information section
        layerInfo.putLong();

        //---Layer info---
        //Length of the layers info section, rounded up to a multiple of 2
        layerInfo.putLong();
        //Layer count
        layerInfo.putShort((short) 1);

        //---Information about each layer---
        //Rectangle containing the contents of the layer
        layerInfo.putInt(0);
        layerInfo.putInt(0);
        layerInfo.putInt(src.getHeight());
        layerInfo.putInt(src.getWidth());
        //Number of channels in the layer
        layerInfo.putShort((short) model.getComponentSize().length);
        layerInfo.putShort();*/
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bi.createGraphics().drawImage(src, 0, 0, null);

//        LayerMaskWriter layerMaskWriter = new LayerMaskWriter();
//        layerMaskWriter.layers.add(new Layer(src, Settings.PsbWriter_LayerName.getValue()));
        ImageDataWriter idw = new ImageDataWriter(src);
        try {
//            irw.writeBytes(dst);
//            layerMaskWriter.writeBytes(dst);
            idw.writeBytes(dst);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getExtension() {
        return "psb";
    }

    @Override
    public String getDescription() {
        return L.get("imgWriter.PsbWriter.description");
    }

    @Override
    public Component getOptionsPanel() {
        return options.$$$getRootComponent$$$();
    }
}