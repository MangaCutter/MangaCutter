package net.macu.UI;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class IconManager {

    private static BufferedImage brandIcon = null;
    private static ImageIcon searchIcon = null;
    private static ImageIcon arrowUpIcon = null;
    private static ImageIcon arrowDownIcon = null;
    private static ImageIcon sortIcon = null;

    public static void loadIcons() {
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        TranscoderImpl transcoder = new TranscoderImpl();
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, factory.getDOMImplementation(""));
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, false);
        hints.put(ImageTranscoder.KEY_WIDTH, 64.0f);
        hints.put(ImageTranscoder.KEY_HEIGHT, 64.0f);
        transcoder.setTranscodingHints(hints);
        try {
            transcoder.transcode(new TranscoderInput(IconManager.class.getResourceAsStream("MangaCutter.svg")), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        brandIcon = transcoder.getImage();
        hints.put(ImageTranscoder.KEY_WIDTH, 16.0f);
        hints.put(ImageTranscoder.KEY_HEIGHT, 16.0f);
        transcoder.setTranscodingHints(hints);
        try {
            transcoder.transcode(new TranscoderInput(IconManager.class.getResourceAsStream("search_icon.svg")), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        searchIcon = new ImageIcon(transcoder.getImage());
        try {
            transcoder.transcode(new TranscoderInput(IconManager.class.getResourceAsStream("arrow_up.svg")), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        arrowUpIcon = new ImageIcon(transcoder.getImage());
        try {
            transcoder.transcode(new TranscoderInput(IconManager.class.getResourceAsStream("arrow_down.svg")), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        arrowDownIcon = new ImageIcon(transcoder.getImage());
        try {
            transcoder.transcode(new TranscoderInput(IconManager.class.getResourceAsStream("sort.svg")), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        sortIcon = new ImageIcon(transcoder.getImage());
    }

    public static BufferedImage getBrandIcon() {
        return brandIcon;
    }

    public static ImageIcon getSearchIcon() {
        return searchIcon;
    }

    public static ImageIcon getArrowUpIcon() {
        return arrowUpIcon;
    }

    public static ImageIcon getArrowDownIcon() {
        return arrowDownIcon;
    }

    public static ImageIcon getSortIcon() {
        return sortIcon;
    }

    public static ImageIcon getSpinnerIcon(int width, int height) {
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        TranscoderImpl transcoder = new TranscoderImpl();
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, factory.getDOMImplementation(""));
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, false);
        hints.put(ImageTranscoder.KEY_WIDTH, (float) width);
        hints.put(ImageTranscoder.KEY_HEIGHT, (float) height);
        transcoder.setTranscodingHints(hints);
        try {
            transcoder.transcode(new TranscoderInput(IconManager.class.getResourceAsStream("spinner.svg")), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        return new ImageIcon(transcoder.getImage());
    }
}
