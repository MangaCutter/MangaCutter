package net.macu.UI;

import net.macu.core.TranscoderImpl;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class IconManager {

    private static BufferedImage brandImage = null;
    private static ImageIcon searchIcon = null;
    private static ImageIcon arrowUpIcon = null;
    private static ImageIcon arrowDownIcon = null;
    private static ImageIcon sortIcon = null;
    private static ImageIcon clearIcon = null;

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
        brandImage = transcoder.transcodeImage("MangaCutter.svg");
        hints.put(ImageTranscoder.KEY_WIDTH, 16.0f);
        hints.put(ImageTranscoder.KEY_HEIGHT, 16.0f);
        transcoder.setTranscodingHints(hints);
        searchIcon = transcoder.transcodeIcon("search_icon.svg");
        arrowUpIcon = transcoder.transcodeIcon("arrow_up.svg");
        arrowDownIcon = transcoder.transcodeIcon("arrow_down.svg");
        sortIcon = transcoder.transcodeIcon("sort.svg");
        clearIcon = transcoder.transcodeIcon("clear.svg");
    }

    public static BufferedImage getBrandImage() {
        return brandImage;
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
        return transcoder.transcodeIcon("spinner.svg");
    }

    public static Icon getClearIcon() {
        return clearIcon;
    }
}
