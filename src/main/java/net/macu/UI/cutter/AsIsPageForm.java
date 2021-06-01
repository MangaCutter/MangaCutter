package net.macu.UI.cutter;

import net.macu.cutter.AsIsCutter;
import net.macu.cutter.Cutter;
import net.macu.settings.L;

import javax.swing.*;

public class AsIsPageForm implements Form {

    @Override
    public boolean validateInput() {
        return true;
    }

    @Override
    public JComponent getRootComponent() {
        return new JPanel();
    }

    public String getDescription() {
        return L.get("UI.cutter.AsIsPageForm.description");
    }

    @Override
    public Cutter createPreparedCutter() {
        return new AsIsCutter();
    }

    @Override
    public boolean isReturnsSingleFile() {
        return false;
    }
}
