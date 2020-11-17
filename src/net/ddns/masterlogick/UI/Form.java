package net.ddns.masterlogick.UI;

import net.ddns.masterlogick.core.Pipeline;

import javax.swing.*;
import java.util.HashMap;

public interface Form {
    boolean validateInput();

    JComponent getRootComponent();

    String getDescription();

    Pipeline getConfiguredPipeline();
}
