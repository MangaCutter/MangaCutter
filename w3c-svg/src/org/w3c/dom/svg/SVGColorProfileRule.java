
package org.w3c.dom.svg;

import org.w3c.dom.DOMException;

public interface SVGColorProfileRule extends
        SVGCSSRule,
        SVGRenderingIntent {
    String getSrc();

    void setSrc(String src)
            throws DOMException;

    String getName();

    void setName(String name)
            throws DOMException;

    short getRenderingIntent();

    void setRenderingIntent(short renderingIntent)
            throws DOMException;
}
