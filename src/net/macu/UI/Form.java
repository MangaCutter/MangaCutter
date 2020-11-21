package net.macu.UI;

import net.macu.core.Pipeline;

import javax.swing.*;

public interface Form {
    boolean validateInput();

    JComponent getRootComponent();

    String getDescription();

    Pipeline getConfiguredPipeline();
}
