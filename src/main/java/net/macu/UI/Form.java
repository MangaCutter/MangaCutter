package net.macu.UI;

import javax.swing.*;

public interface Form {

    boolean validateInput();

    JComponent getRootComponent();

    String getName();
}
