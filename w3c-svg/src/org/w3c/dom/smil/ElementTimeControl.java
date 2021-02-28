package org.w3c.dom.smil;

public interface ElementTimeControl {
    void beginElement();

    void beginElementAt(float offset);

    void endElement();

    void endElementAt(float offset);

}