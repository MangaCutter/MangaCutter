package net.macu.UI;

import net.macu.settings.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;

public class SettingsFrame {
    private static SettingsFrame frame = null;
    private final JButton applyButton;
    private final JFrame jframe;

    private final HashMap<Setting, Object> scheduledChanges = new HashMap<>();

    private SettingsFrame() {
        jframe = History.createJFrameFromHistory("UI.SettingsFrame.frame_title", 540, 350);
        jframe.setTitle(L.get("UI.SettingsFrame.frame_title"));
        applyButton = new JButton(L.get("UI.SettingsFrame.apply_button"));
        applyButton.setEnabled(false);
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        JPanel viewportRoot = new JPanel();
        JScrollPane scrollPane = new JScrollPane(viewportRoot);
        scrollPane.getVerticalScrollBar().setUnitIncrement(Settings.ViewManager_MasterScrollSpeed.getValue());
        viewportRoot.setLayout(new BoxLayout(viewportRoot, BoxLayout.PAGE_AXIS));
        List<Setting> allParameters = Settings.getAllSettings();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        String category = getCategoryName(allParameters.get(0).getName());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 0, 4, 0),
                BorderFactory.createTitledBorder(L.get(category))));
        for (Setting setting : allParameters) {
            if (!getCategoryName(setting.getName()).equals(category)) {
                viewportRoot.add(panel);
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                category = getCategoryName(setting.getName());
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(4, 0, 4, 0),
                        BorderFactory.createTitledBorder(L.get(category))));
            }
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            JPanel left = new JPanel();
            left.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            left.add(new JLabel(L.get(setting.getName())));
            JPanel right = new JPanel();
            right.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            if (setting instanceof BooleanSetting) {
                JCheckBox jcb = new JCheckBox();
                jcb.setSelected(((BooleanSetting) setting).getValue());
                jcb.addActionListener(e -> {
                    scheduledChanges.put(setting, jcb.isSelected());
                    applyButton.setEnabled(true);
                });
                right.add(jcb);
            } else if (setting instanceof IntSetting) {
                JTextField intField = new JTextField(10);
                intField.setText(String.valueOf(((IntSetting) setting).getValue()));
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
                        scheduledChanges.put(setting, intField.getText());
                        applyButton.setEnabled(true);
                    }
                });
                right.add(intField);
            } else if (setting instanceof StringSetting) {
                JTextField stringField = new JTextField(14);
                stringField.setText(((StringSetting) setting).getValue());
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
                        scheduledChanges.put(setting, stringField.getText());
                        applyButton.setEnabled(true);
                    }
                });
                right.add(stringField);
            } else if (setting instanceof ListSetting) {
                JComboBox<String> comboboxField = new JComboBox<>(((ListSetting) setting).getVariants());
                comboboxField.setSelectedItem(setting.getValue());
                comboboxField.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        scheduledChanges.put(setting, e.getItem());
                        applyButton.setEnabled(true);
                    }
                });
                comboboxField.setRenderer(((ListSetting) setting).getRenderer());
                right.add(comboboxField);
            }
            p.add(left, BorderLayout.WEST);
            p.add(right, BorderLayout.EAST);
            panel.add(p);
        }
        viewportRoot.add(panel);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        applyButton.addActionListener(e -> applyScheduledSettings());
        JButton cancelButton = new JButton(L.get("UI.SettingsFrame.cancel_button"));
        cancelButton.addActionListener(e -> {
            discardScheduledSettings();
            jframe.setVisible(false);
        });
        JButton okButton = new JButton(L.get("UI.SettingsFrame.ok_button"));
        okButton.addActionListener(e -> {
            applyScheduledSettings();
            jframe.setVisible(false);
        });
        p.add(okButton);
        p.add(cancelButton);
        p.add(applyButton);
        root.add(scrollPane);
        root.add(p);
        jframe.setIconImage(IconManager.getBrandIcon());
        jframe.setContentPane(root);
        jframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                discardScheduledSettings();
                jframe.setVisible(false);
            }
        });
        jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    public static void openFrame() {
        if (frame == null) frame = new SettingsFrame();
        frame.jframe.setVisible(true);
    }

    private void discardScheduledSettings() {
        scheduledChanges.clear();
        applyButton.setEnabled(false);
    }

    private static String getCategoryName(String s) {
        return s.substring(0, s.lastIndexOf("."));
    }

    private void applyScheduledSettings() {
        scheduledChanges.forEach((parameter, o) -> {
            if (parameter instanceof IntSetting) {
                parameter.setValue(Integer.parseInt((String) o));//todo add NumberFormatException try-catch block
            } else if (parameter instanceof BooleanSetting) {
                parameter.setValue(o);
            } else if (parameter instanceof StringSetting) {
                parameter.setValue(o);
            } else if (parameter instanceof ListSetting) {
                parameter.setValue(o);
            }
        });
        scheduledChanges.clear();
        applyButton.setEnabled(false);
    }
}
