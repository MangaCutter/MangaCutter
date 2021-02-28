
package org.w3c.dom.svg;

import org.w3c.dom.DOMException;

public interface SVGLangSpace {
    String getXMLlang();

    void setXMLlang(String xmllang)
            throws DOMException;

    String getXMLspace();

    void setXMLspace(String xmlspace)
            throws DOMException;
}
