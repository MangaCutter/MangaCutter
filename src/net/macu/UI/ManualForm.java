package net.macu.UI;

import net.macu.cutter.Cutter;
import net.macu.cutter.manual.ManualCutter;
import net.macu.settings.L;

import javax.swing.*;

public class ManualForm implements Form {

    @Override
    public boolean validateInput() {
        return true;
    }

    @Override
    public JComponent getRootComponent() {
        return new JPanel();
    }

    public String getDescription() {
        return L.get("UI.ManualForm.description");
    }

    @Override
    public Cutter createPreparedCutter() {
        return new ManualCutter();
    }

    @Override
    public boolean isReturnsSingleFile() {
        return false;
    }
}
