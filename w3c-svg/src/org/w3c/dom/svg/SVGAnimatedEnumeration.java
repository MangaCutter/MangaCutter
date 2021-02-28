
package org.w3c.dom.svg;

import org.w3c.dom.DOMException;

public interface SVGAnimatedEnumeration {
    short getBaseVal();

    void setBaseVal(short baseVal)
            throws DOMException;

    short getAnimVal();
}
