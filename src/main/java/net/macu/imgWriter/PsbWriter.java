package net.macu.imgWriter;

import net.macu.UI.Form;
import net.macu.UI.imgWriter.PsdForm;
import net.macu.imgWriter.psd.ColorMode;
import net.macu.settings.L;
import net.macu.util.ExtendedByteArrayOutputStream;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class PsbWriter implements ImgWriter {
    private static final byte[] SIGNATURE_8BPS = new byte[]{0x38, 0x42, 0x50, 0x53};
    private static final byte[] SIGNATURE_8BIM = new byte[]{0x38, 0x42, 0x49, 0x4d};
    private static final byte[] SIGNATURE_norm = new byte[]{0x6e, 0x6f, 0x72, 0x6d};
    private static final byte[] SIGNATURE_luni = new byte[]{0x6c, 0x75, 0x6e, 0x69};
    private static final byte[] SIGNATURE_levl = new byte[]{0x6c, 0x65, 0x76, 0x6c};
    private static final byte[] SIGNATURE_Lvls_ending = new byte[]{0x4c, 0x76, 0x6c, 0x73, 0x00, 0x03, 0x0, 0x3E};
    private static final byte[] SIGNATURE_ZIP_COMPRESSION = new byte[]{0x00, 0x02};
    private static final byte[] SIGNATURE_RLE_COMPRESSION = new byte[]{0x00, 0x01};
    private static final byte[] THE_FIRST_GRAYSCALE_LEVEL_DESCRIPTION = new byte[]{
            0x00, (byte) 0x96, 0x00, (byte) 0xFF, 0x00, 0x00, 0x00, (byte) 0xFF, 0x00, 0x64
    };
    private static final byte[] USELESS_BUT_REQUIRED_DATA_BLOCK_IN_IMAGE_LAYER = new byte[]{0x38, 0x42, 0x49, 0x4D,
            0x6C, 0x6E, 0x73, 0x72, 0x00, 0x00, 0x00, 0x04, 0x6C, 0x61, 0x79, 0x72, 0x38, 0x42, 0x49, 0x4D, 0x6C, 0x79,
            0x69, 0x64, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x02, 0x38, 0x42, 0x49, 0x4D, 0x63, 0x6C, 0x62, 0x6C,
            0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x69, 0x6E, 0x66, 0x78, 0x00, 0x00,
            0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x6B, 0x6E, 0x6B, 0x6F, 0x00, 0x00, 0x00, 0x04,
            0x00, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x6C, 0x73, 0x70, 0x66, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00,
            0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x6C, 0x63, 0x6C, 0x72, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x73, 0x68, 0x6D, 0x64, 0x00, 0x00, 0x00, 0x48, 0x00, 0x00,
            0x00, 0x01, 0x38, 0x42, 0x49, 0x4D, 0x63, 0x75, 0x73, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x34,
            0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x6D, 0x65, 0x74, 0x61,
            0x64, 0x61, 0x74, 0x61, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x09, 0x6C, 0x61, 0x79, 0x65, 0x72, 0x54,
            0x69, 0x6D, 0x65, 0x64, 0x6F, 0x75, 0x62, 0x41, (byte) 0xD8, 0x32, (byte) 0xD7, (byte) 0xB5, (byte) 0xAB,
            0x39, 0x2B, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x66, 0x78, 0x72, 0x70, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] USELESS_BUT_REQUIRED_DATA_BLOCK_IN_LEVELS_LAYER = new byte[]{0x38, 0x42, 0x49, 0x4D,
            0x6C, 0x6E, 0x73, 0x72, 0x00, 0x00, 0x00, 0x04, 0x63, 0x6F, 0x6E, 0x74, 0x38, 0x42, 0x49, 0x4D, 0x6C, 0x79,
            0x69, 0x64, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x03, 0x38, 0x42, 0x49, 0x4D, 0x63, 0x6C, 0x62, 0x6C,
            0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x69, 0x6E, 0x66, 0x78, 0x00, 0x00,
            0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x6B, 0x6E, 0x6B, 0x6F, 0x00, 0x00, 0x00, 0x04,
            0x00, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x6C, 0x73, 0x70, 0x66, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00,
            0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x6C, 0x63, 0x6C, 0x72, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x73, 0x68, 0x6D, 0x64, 0x00, 0x00, 0x00, 0x48, 0x00, 0x00,
            0x00, 0x01, 0x38, 0x42, 0x49, 0x4D, 0x63, 0x75, 0x73, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x34,
            0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x6D, 0x65, 0x74, 0x61,
            0x64, 0x61, 0x74, 0x61, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x09, 0x6C, 0x61, 0x79, 0x65, 0x72, 0x54,
            0x69, 0x6D, 0x65, 0x64, 0x6F, 0x75, 0x62, 0x41, (byte) 0xD8, 0x32, (byte) 0xD7, (byte) 0xB5, (byte) 0xAB,
            0x39, 0x2B, 0x00, 0x38, 0x42, 0x49, 0x4D, 0x66, 0x78, 0x72, 0x70, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] BLENDING_RANGES_DATA = new byte[]{0x00, 0x00, 0x00, 0x28,
            0x00, 0x00, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
            0x00, 0x00, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
            0x00, 0x00, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
            0x00, 0x00, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
            0x00, 0x00, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF};
    private static final byte[] LEVELS_LAYER_MASK_DATA = new byte[]{0x00, 0x00, 0x00, 0x14, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            (byte) 0xFF, 0x00, 0x00, 0x00};
    private static final short BIT_DEPTH = 8;
    private final PsdForm form;

    public PsbWriter() {
        form = new PsdForm();
    }

    @Override
    public void writeImage(BufferedImage src, OutputStream dst) {
        BufferedImage prepared = createPreparedImage(src);
        int height = prepared.getHeight();
        int width = prepared.getWidth();
        try {
            ExtendedByteArrayOutputStream channelsImageData = new ExtendedByteArrayOutputStream();
            long[] channelsSizes = writeChannelsData(channelsImageData, prepared, width, height);
            //---File header---
            ByteBuffer header = ByteBuffer.allocate(34);
            header.order(ByteOrder.BIG_ENDIAN);
            //Signature: always equal to '8BPS'
            header.put(SIGNATURE_8BPS);
            //**PSB** version is 2
            header.putShort((short) 2);
            //Reserved: must be zero
            header.put(new byte[6]);
            //The number of channels in the image, including any alpha channels
            header.putShort((short) (form.getNumberOfChannels() - 1));
            //The height of the image in pixels
            header.putInt(height);
            //The width of the image in pixels
            header.putInt(width);
            //Depth: the number of bits per channel
            header.putShort(BIT_DEPTH);
            //The color mode of the file
            header.putShort(form.getImageColorModeValue());

            //---Color mode data---
            //---Image resources---
            //zeroed section lengths
            header.put(new byte[8]);
            dst.write(header.array());
            int imageLayerExtraFieldsLength = 4 + BLENDING_RANGES_DATA.length + getPascalStringLength(form.getImageLayerName()) +
                    SIGNATURE_8BIM.length + SIGNATURE_luni.length + getUnicodeStringLength(form.getImageLayerName()) + USELESS_BUT_REQUIRED_DATA_BLOCK_IN_IMAGE_LAYER.length;
            int imageLayerLength = 4 * 4 + 2 + 10 * form.getNumberOfChannels() + SIGNATURE_8BIM.length +
                    SIGNATURE_norm.length + 4 + 4 + imageLayerExtraFieldsLength;
            int levelsLayerExtraFieldsLength = LEVELS_LAYER_MASK_DATA.length + BLENDING_RANGES_DATA.length + getPascalStringLength(form.getLevelsLayerName()) +
                    SIGNATURE_8BIM.length + SIGNATURE_luni.length + getUnicodeStringLength(form.getLevelsLayerName()) +
                    USELESS_BUT_REQUIRED_DATA_BLOCK_IN_LEVELS_LAYER.length +
                    SIGNATURE_8BIM.length + SIGNATURE_levl.length + 4 + 2 + 62 * 5 * 2 + SIGNATURE_Lvls_ending.length + 2;
            int levelsLayerLength = 4 * 4 + 2 + 10 * (form.getNumberOfChannels() + 1) + SIGNATURE_8BIM.length +
                    SIGNATURE_norm.length + 4 + 4 + levelsLayerExtraFieldsLength;
            long imageChannelsDataLength = channelsImageData.getSize();

            int layerInfoLength = 2 + imageLayerLength + (form.hasLevelsLayer() ? levelsLayerLength : 0);

            //---Layer and mask information---
            ByteBuffer layerInfo = ByteBuffer.allocate(8 + 8 + layerInfoLength - (form.hasLevelsLayer() ? levelsLayerLength : 0));
            layerInfo.order(ByteOrder.BIG_ENDIAN);

            //Length of the layer and mask information section
            layerInfo.putLong(8 + layerInfoLength + imageChannelsDataLength + 4 + (layerInfoLength + imageChannelsDataLength) % 2);

            //---Layer info---
            //Length of the layers info section, rounded up to a multiple of 2
            layerInfo.putLong(layerInfoLength + imageChannelsDataLength + (layerInfoLength + imageChannelsDataLength) % 2);
            //Layer count
            layerInfo.putShort((short) (form.hasLevelsLayer() ? 2 : 1));

            //---Layer record---
            //Rectangle containing the contents of the layer
            layerInfo.putInt(0);
            layerInfo.putInt(0);
            layerInfo.putInt(height);
            layerInfo.putInt(width);
            //Number of channels in the layer
            layerInfo.putShort(form.getNumberOfChannels());
            // Channel information. Ten bytes per channel, consisting of:
            //2 bytes for Channel ID: 0 = red, 1 = green, etc.;
            //-1 = transparency mask; -2 = user supplied layer mask, -3 real user supplied layer mask (when both a user mask and a vector mask are present)
            //8 bytes for length of corresponding channel data.
            for (short i = -1; i < form.getNumberOfChannels() - 1; i++) {
                layerInfo.putShort(i);
                layerInfo.putLong(channelsSizes[i + 1]);
            }
            //Blend mode signature: '8BIM'
            layerInfo.put(SIGNATURE_8BIM);
            //Blend mode key: 'norm' = normal
            layerInfo.put(SIGNATURE_norm);
            //Opacity
            layerInfo.put((byte) -1);
            //Clipping
            layerInfo.put((byte) 0);
            //Flags:
            //bit 0 = transparency protected
            //bit 1 = visible
            //bit 2 = obsolete
            //bit 3 = 1 for Photoshop 5.0 and later, tells if bit 4 has useful information
            //bit 4 = pixel data irrelevant to appearance of document
            layerInfo.put((byte) 0x08);
            //Filler (zero)
            layerInfo.put((byte) 0);
            //Length of the extra data field
            layerInfo.putInt(imageLayerExtraFieldsLength);

            //---Layer mask / adjustment layer data---
            //Size of the data
            layerInfo.putInt(0);

            //---Layer blending ranges data---
            // Length of layer blending ranges data
            layerInfo.put(BLENDING_RANGES_DATA);

            //Layer name: Pascal string, padded to a multiple of 4 bytes
            writePascalString(form.getImageLayerName(), layerInfo);

            //---Series of tagged blocks containing various types of data---
            //---Unicode layer name---
            layerInfo.put(SIGNATURE_8BIM);
            layerInfo.put(SIGNATURE_luni);
            writeUnicodeString(form.getImageLayerName(), layerInfo);
            layerInfo.put(USELESS_BUT_REQUIRED_DATA_BLOCK_IN_IMAGE_LAYER);
            dst.write(layerInfo.array());

            if (form.hasLevelsLayer()) {
                ByteBuffer levelsLayer = ByteBuffer.allocate(levelsLayerLength);
                levelsLayer.order(ByteOrder.BIG_ENDIAN);
                //Rectangle containing the contents of the layer
                levelsLayer.putInt(0);
                levelsLayer.putInt(0);
                levelsLayer.putInt(0);
                levelsLayer.putInt(0);
                //Number of channels in the layer
                levelsLayer.putShort((short) (form.getNumberOfChannels() + 1));
                // Channel information. Ten bytes per channel, consisting of:
                //2 bytes for Channel ID: 0 = red, 1 = green, etc.;
                //-1 = transparency mask; -2 = user supplied layer mask, -3 real user supplied layer mask (when both a user mask and a vector mask are present)
                //8 bytes for length of corresponding channel data.
                for (short i = -2; i < form.getNumberOfChannels() - 1; i++) {
                    levelsLayer.putShort(i);
                    levelsLayer.putLong(channelsSizes[form.getNumberOfChannels() + 2 + i]);
                }
                //Blend mode signature: '8BIM'
                levelsLayer.put(SIGNATURE_8BIM);
                //Blend mode key: 'norm' = normal
                levelsLayer.put(SIGNATURE_norm);
                //Opacity
                levelsLayer.put((byte) -1);
                //Clipping
                levelsLayer.put((byte) 0);
                //Flags:
                //bit 0 = transparency protected;
                //bit 1 = visible;
                //bit 2 = obsolete;
                //bit 3 = 1 for Photoshop 5.0 and later, tells if bit 4 has useful information;
                //bit 4 = pixel data irrelevant to appearance of document
                levelsLayer.put((byte) 0x18);
                //Filler (zero)
                levelsLayer.put((byte) 0);
                //Length of the extra data field
                levelsLayer.putInt(levelsLayerExtraFieldsLength);

                //---Layer mask / adjustment layer data---
                //Size of the data
                levelsLayer.put(LEVELS_LAYER_MASK_DATA);

                //---Layer blending ranges data---
                // Length of layer blending ranges data
                levelsLayer.put(BLENDING_RANGES_DATA);

                //Layer name: Pascal string, padded to a multiple of 4 bytes
                writePascalString(form.getLevelsLayerName(), levelsLayer);

                //---Levels---
                levelsLayer.put(SIGNATURE_8BIM);
                levelsLayer.put(SIGNATURE_levl);
                levelsLayer.putInt(2 + 62 * 5 * 2 + SIGNATURE_Lvls_ending.length + 2);
                levelsLayer.putShort((short) 2);
                if (form.getColorMode() == ColorMode.Grayscale) {
                    levelsLayer.put(THE_FIRST_GRAYSCALE_LEVEL_DESCRIPTION);
                }
                form.getLevels().forEach((psdLevelChannelForm -> {
                    levelsLayer.putShort(psdLevelChannelForm.getInputFloor());
                    levelsLayer.putShort(psdLevelChannelForm.getInputCeiling());
                    levelsLayer.putShort(psdLevelChannelForm.getOutputFloor());
                    levelsLayer.putShort(psdLevelChannelForm.getOutputCeiling());
                    levelsLayer.putShort(psdLevelChannelForm.getGamma());
                }));
                for (int i = form.getLevels().size() + (form.getColorMode() == ColorMode.Grayscale ? 1 : 0); i < 29; i++) {
                    levelsLayer.putShort((short) 0);
                    levelsLayer.putShort((short) 255);
                    levelsLayer.putShort((short) 0);
                    levelsLayer.putShort((short) 255);
                    levelsLayer.putShort((short) 100);
                }
                levelsLayer.put(SIGNATURE_Lvls_ending);
                for (int i = 29; i < 62; i++) {
                    levelsLayer.putShort((short) 0);
                    levelsLayer.putShort((short) 255);
                    levelsLayer.putShort((short) 0);
                    levelsLayer.putShort((short) 255);
                    levelsLayer.putShort((short) 100);
                }
                //Padding???
                levelsLayer.putShort((short) 0);

                //---Series of tagged blocks containing various types of data---
                //---Unicode layer name---
                levelsLayer.put(SIGNATURE_8BIM);
                levelsLayer.put(SIGNATURE_luni);
                writeUnicodeString(form.getLevelsLayerName(), levelsLayer);
                levelsLayer.put(USELESS_BUT_REQUIRED_DATA_BLOCK_IN_LEVELS_LAYER);

                dst.write(levelsLayer.array());
            }

            //---Channel image data---
            channelsImageData.writeTo(dst);

            //---Layers info section padding byte
            if ((layerInfoLength + imageChannelsDataLength) % 2 != 0) dst.write(0);
            //---Global mask---
            dst.write(new byte[4]);

            dst.write(SIGNATURE_RLE_COMPRESSION);
            byte[] rleScanline = new byte[2 * (width / 128 + (width % 128 != 0 ? 1 : 0))];
            for (int i = 0; i < width / 128; i++) {
                rleScanline[2 * i] = (byte) 0x81;
                rleScanline[2 * i + 1] = (byte) 0xff;
            }
            if (width % 128 != 0) {
                rleScanline[rleScanline.length - 2] = (byte) (1 - width % 128);
                rleScanline[rleScanline.length - 1] = (byte) 0xff;
            }
            int heightSum = height * form.getNumberOfChannels();
            ByteBuffer rleHeader = ByteBuffer.allocate(4 * heightSum);
            rleHeader.order(ByteOrder.BIG_ENDIAN);
            for (int i = 0; i < heightSum; i++) {
                rleHeader.putInt(rleScanline.length);
            }
            dst.write(rleHeader.array());
            for (int i = 0; i < heightSum; i++) {
                dst.write(rleScanline);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage createPreparedImage(BufferedImage src) {
        ColorSpace colorSpace = ColorSpace.getInstance(form.getColorMode() == ColorMode.Grayscale ?
                ColorSpace.CS_GRAY : ColorSpace.CS_sRGB);
        int[] bits = new int[form.getNumberOfChannels() - 1];
        Arrays.fill(bits, BIT_DEPTH);
        ColorModel colorModel = new ComponentColorModel(colorSpace, bits, false, false,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage dst = new BufferedImage(colorModel,
                colorModel.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
                colorModel.isAlphaPremultiplied(), null);
        dst.createGraphics().drawImage(src, 0, 0, null);
        return dst;
    }

    private int getPascalStringLength(String s) {
        byte[] stringBytes;
        while ((stringBytes = s.getBytes(StandardCharsets.UTF_8)).length > Byte.MAX_VALUE) {
            s = s.substring(0, s.length() - 1);
        }
        return (stringBytes.length / 4 + 1) * 4;// == ((stringBytes.length + 1 + 3) / 4) * 4
    }

    private void writePascalString(String s, ByteBuffer buff) {
        byte[] stringBytes;
        while ((stringBytes = s.getBytes(StandardCharsets.UTF_8)).length > Byte.MAX_VALUE) {
            s = s.substring(0, s.length() - 1);
        }
        buff.put((byte) stringBytes.length);
        buff.put(stringBytes);
        for (int i = 0; (1 + stringBytes.length + i) % 4 != 0; i++) {
            buff.put((byte) 0);
        }
    }

    private int getUnicodeStringLength(String s) {
        return s.getBytes(StandardCharsets.UTF_16BE).length + 4 + 4;
    }

    private void writeUnicodeString(String s, ByteBuffer buff) {
        byte[] data = s.getBytes(StandardCharsets.UTF_16BE);
        buff.putInt(4 + data.length);
        buff.putInt(s.length());
        buff.put(data);
    }

    private long[] writeChannelsData(ExtendedByteArrayOutputStream out, BufferedImage prepared, int width, int height) throws IOException {
        //no. - RGB_channel                 Grayscale_channel
        //if there is no levels layer
        //0 - Alpha channel                 Alpha channel
        //1 - Red                           Grayscale
        //2 - Green                         No
        //3 - Blue                          No
        //if there is levels layer
        //0 - Alpha channel                 Alpha channel
        //1 - Red                           Grayscale
        //2 - Green                         Levels' user mask channel
        //3 - Blue                          Levels' alpha channel
        //4 - Levels' user mask channel     Levels' grayscale
        //5 - Levels' alpha channel         No
        //6 - Levels' red                   No
        //7 - Levels' green                 No
        //8 - Levels' blue                  No
        //---Channel image data---
        int realNumberOfChannels = form.getNumberOfChannels() - 1;
        long[] sizes = new long[1 + realNumberOfChannels + (form.hasLevelsLayer() ? 2 + realNumberOfChannels : 0)];
        {
            //write image alpha data(all 0xff)
            out.write(SIGNATURE_ZIP_COMPRESSION);
            byte[] scanline = new byte[prepared.getWidth()];
            Arrays.fill(scanline, (byte) 0xff);
            Deflater dfl = new Deflater(Deflater.DEFAULT_COMPRESSION, false);
            DeflaterOutputStream dos = new DeflaterOutputStream(out, dfl);
            for (int i = 0; i < prepared.getHeight(); i++) {
                dos.write(scanline);
            }
            dos.finish();
            sizes[0] = out.getSize();
        }

        DataBufferByte imageData = (DataBufferByte) prepared.getRaster().getDataBuffer();
        byte[][] rawImgData = imageData.getBankData();
        byte[] channelData = new byte[width];
        long lastSize = sizes[0];
        for (int i = 0; i < realNumberOfChannels; i++) {
            int index = 0;
            long offset = 0;
            out.write(SIGNATURE_ZIP_COMPRESSION);
            Deflater dfl = new Deflater(Deflater.DEFAULT_COMPRESSION, false);
            DeflaterOutputStream dos = new DeflaterOutputStream(out, dfl);
            for (long j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    long l = realNumberOfChannels * (j * width + k) + i;
                    if (l - offset >= rawImgData[index].length) {
                        offset += rawImgData[index].length;
                        index++;
                    }
                    channelData[k] = rawImgData[index][(int) (l - offset)];
                }
                dos.write(channelData);
            }
            dos.finish();
            sizes[1 + i] = out.getSize() - lastSize;
            lastSize = out.getSize();
        }
        if (form.hasLevelsLayer()) {
            for (int i = 0; i < 2 + realNumberOfChannels; i++) {
                out.write(new byte[2]);
                sizes[1 + realNumberOfChannels + i] = 2L;
            }
        }
        return sizes;
    }

    @Override
    public String getExtension() {
        return "psd";
    }

    @Override
    public String getDescription() {
        return L.get("imgWriter.PsbWriter.description");
    }

    @Override
    public Form getOptionsForm() {
        return form;
    }
}