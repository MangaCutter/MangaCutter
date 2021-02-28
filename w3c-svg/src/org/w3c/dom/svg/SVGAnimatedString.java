
package org.w3c.dom.svg;

import org.w3c.dom.DOMException;

public interface SVGAnimatedString {
    String getBaseVal();

    void setBaseVal(String baseVal)
            throws DOMException;

    String getAnimVal();
}
