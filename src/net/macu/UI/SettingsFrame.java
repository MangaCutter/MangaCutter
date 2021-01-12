package net.macu.UI;

import net.macu.settings.L;
import net.macu.settings.Parameter;
import net.macu.settings.Parameters;
import net.macu.settings.Settings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SettingsFrame extends JFrame {
    private static SettingsFrame frame = null;
    private final JButton applyButton;

    private final HashMap<Parameter, Object> scheduledChanges = new HashMap<>();

    private SettingsFrame() {
        super(L.get("UI.SettingsFrame.frame_title"));
        applyButton = new JButton(L.get("UI.SettingsFrame.apply_button"));
        applyButton.setEnabled(false);
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
            panel.setBorder(BorderFactory.createTitledBorder(L.get(parameters.getName())));//todo add empty border
            parameters.sort(Comparator.comparing(Parameter::getType));
            parameters.forEach(parameter -> {
                JPanel p = new JPanel();
                p.setLayout(new BorderLayout());
                JPanel left = new JPanel();
                left.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
                left.add(new JLabel(L.get(parameter.getName())));
                JPanel right = new JPanel();
                right.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
                switch (parameter.getType()) {
                    case STRING_TYPE:
                        JTextField stringField = new JTextField(14);
                        stringField.setText(parameter.getString());
                        stringField.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                changedUpdate(e);
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                changedUpdate(e);
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                scheduledChanges.put(parameter, stringField.getText());
                                applyButton.setEnabled(true);
                            }
                        });
                        right.add(stringField);
                        break;
                    case INT_TYPE:
                        JTextField intField = new JTextField(10);
                        intField.setText(String.valueOf(parameter.getInt()));
                        intField.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                changedUpdate(e);
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                changedUpdate(e);
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                scheduledChanges.put(parameter, intField.getText());
                                applyButton.setEnabled(true);
                            }
                        });
                        right.add(intField);
                        break;
                    case BOOLEAN_TYPE:
                        JCheckBox jcb = new JCheckBox();
                        jcb.setSelected(parameter.getBoolean());
                        jcb.addActionListener(e -> {
                            scheduledChanges.put(parameter, jcb.isSelected());
                            applyButton.setEnabled(true);
                        });
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
        applyButton.addActionListener(e -> applyScheduledSettings());
        JButton cancelButton = new JButton(L.get("UI.SettingsFrame.cancel_button"));
        cancelButton.addActionListener(e -> {
            discardScheduledSettings();
            setVisible(false);
        });
        JButton okButton = new JButton(L.get("UI.SettingsFrame.ok_button"));
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
        setLocationRelativeTo(null);
        pack();
        setSize(new Dimension(Math.max(getWidth(), 540), Math.min(getHeight(), 350)));
    }

    public static void openFrame() {
        if (frame == null) frame = new SettingsFrame();
        frame.setVisible(true);
    }

    private void discardScheduledSettings() {
        scheduledChanges.clear();
        applyButton.setEnabled(false);
    }

    private void applyScheduledSettings() {
        scheduledChanges.forEach((parameter, o) -> {
            if (parameter.getType() == Parameter.Type.INT_TYPE) {
                parameter.setValue(Integer.parseInt((String) o));//todo add NumberFormatException try-catch block
            } else {
                parameter.setValue(o);
            }
        });
        scheduledChanges.clear();
        applyButton.setEnabled(false);
    }
}
