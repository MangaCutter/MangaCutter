
package org.w3c.dom.svg;

import org.w3c.dom.DOMException;

public interface SVGPathSegList {
    int getNumberOfItems();

    void clear()
            throws DOMException;

    SVGPathSeg initialize(SVGPathSeg newItem)
            throws DOMException, SVGException;

    SVGPathSeg getItem(int index)
            throws DOMException;

    SVGPathSeg insertItemBefore(SVGPathSeg newItem, int index)
            throws DOMException, SVGException;

    SVGPathSeg replaceItem(SVGPathSeg newItem, int index)
            throws DOMException, SVGException;

    SVGPathSeg removeItem(int index)
            throws DOMException;

    SVGPathSeg appendItem(SVGPathSeg newItem)
            throws DOMException, SVGException;
}
