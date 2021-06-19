package net.macu.UI;

import javax.swing.*;

public class EmptyForm implements Form {
    @Override
    public boolean validateInput() {
        return true;
    }

    @Override
    public JComponent getRootComponent() {
        return new JPanel();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void saveChoice() {

    }
}
