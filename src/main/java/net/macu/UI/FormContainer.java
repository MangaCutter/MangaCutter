package net.macu.UI;

import javax.swing.*;
import java.awt.*;

public class FormContainer extends JPanel {
    private Form form;

    public FormContainer() {
        setLayout(new BorderLayout(0, 0));
    }

    public void setForm(Form form) {
        this.form = form;
        String description = form.getName();
        if (!description.isEmpty()) {
            setBorder(BorderFactory.createTitledBorder(description));
        } else {
            setBorder(BorderFactory.createEmptyBorder());
        }
        removeAll();
        add(form.getRootComponent());
    }

    public boolean validateInput() {
        return form.validateInput();
    }
}
