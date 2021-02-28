
package org.w3c.dom.svg;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public interface SVGElement extends
        Element {
    String getId();

    void setId(String id)
            throws DOMException;

    String getXMLbase();

    void setXMLbase(String xmlbase)
            throws DOMException;

    SVGSVGElement getOwnerSVGElement();

    SVGElement getViewportElement();
}
