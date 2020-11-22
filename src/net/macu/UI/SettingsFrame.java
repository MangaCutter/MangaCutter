package net.macu.UI;

import net.macu.settings.Parameter;
import net.macu.settings.Parameters;
import net.macu.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettingsFrame extends JDialog {

    private final ArrayList<ScheduledChange> scheduledChanges = new ArrayList<>();

    public SettingsFrame() {
        super(ViewManager.getFrame(), "Manga Cutter");
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        JPanel viewportRoot = new JPanel();
        JScrollPane scrollPane = new JScrollPane(viewportRoot);
        viewportRoot.setLayout(new BoxLayout(viewportRoot, BoxLayout.PAGE_AXIS));
        List<Parameters> allParameters = Settings.getAllParameters();
        allParameters.sort(Comparator.comparing(Parameters::getName));
        allParameters.forEach(parameters -> {
            if (parameters.isEmpty()) return;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createTitledBorder(parameters.getName()));
            parameters.sort(Comparator.comparing(Parameter::getType));
            parameters.forEach(parameter -> {
                JPanel p = new JPanel();
                p.setLayout(new BorderLayout());
                JPanel left = new JPanel();
                left.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
                left.add(new JLabel(parameter.getName()));
                JPanel right = new JPanel();
                right.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
                switch (parameter.getType()) {
                    case STRING_TYPE:
                        JTextField stringField = new JTextField(14);
                        stringField.setText(parameter.getString());
                        right.add(stringField);
                        break;
                    case INT_TYPE:
                        JTextField intField = new JTextField(10);
                        intField.setText(String.valueOf(parameter.getInt()));
                        right.add(intField);
                        break;
                    case BOOLEAN_TYPE:
                        JCheckBox jcb = new JCheckBox();
                        jcb.setSelected(parameter.getBoolean());
                        right.add(jcb);
                        break;
                }
                p.add(left, BorderLayout.WEST);
                p.add(right, BorderLayout.EAST);
                panel.add(p);
            });
            viewportRoot.add(panel);
        });
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton applyButton = new JButton("Применить");
        applyButton.addActionListener(e -> applyScheduledSettings());
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> discardScheduledSettings());
        JButton okButton = new JButton("ОК");
        okButton.addActionListener(e -> {
            applyScheduledSettings();
            setVisible(false);
        });
        p.add(okButton);
        p.add(cancelButton);
        p.add(applyButton);
        root.add(scrollPane);
        root.add(p);
        setContentPane(root);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                discardScheduledSettings();
                setVisible(false);
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(ViewManager.getFrame());
        pack();
        setSize(new Dimension(540, Math.min(getHeight(), 350)));
        setResizable(false);
    }

    private void discardScheduledSettings() {
        scheduledChanges.clear();
    }

    private void applyScheduledSettings() {
        scheduledChanges.forEach(scheduledChange -> scheduledChange.p.setValue(scheduledChange.newValue));
    }

    private class ScheduledChange {
        Parameter p;
        Object newValue;
    }
}
