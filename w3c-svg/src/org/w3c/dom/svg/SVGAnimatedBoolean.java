
package org.w3c.dom.svg;

import org.w3c.dom.DOMException;

public interface SVGAnimatedBoolean {
    boolean getBaseVal();

    void setBaseVal(boolean baseVal)
            throws DOMException;

    boolean getAnimVal();
}
