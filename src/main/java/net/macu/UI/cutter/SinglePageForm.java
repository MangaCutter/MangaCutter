package net.macu.UI.cutter;

import net.macu.cutter.Cutter;
import net.macu.cutter.SingleScanCutter;
import net.macu.settings.L;

import javax.swing.*;

public class SinglePageForm implements Form {

    @Override
    public boolean validateInput() {
        return true;
    }

    @Override
    public JComponent getRootComponent() {
        return new JPanel();
    }

    public String getDescription() {
        return L.get("UI.cutter.SinglePageForm.description");
    }

    @Override
    public Cutter createPreparedCutter() {
        return new SingleScanCutter();
    }

    @Override
    public boolean isReturnsSingleFile() {
        return true;
    }
}
