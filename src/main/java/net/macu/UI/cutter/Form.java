package net.macu.UI.cutter;

import net.macu.cutter.Cutter;

import javax.swing.*;

public interface Form {
    boolean validateInput();

    JComponent getRootComponent();

    String getDescription();

    Cutter createPreparedCutter();

    boolean isReturnsSingleFile();
}
