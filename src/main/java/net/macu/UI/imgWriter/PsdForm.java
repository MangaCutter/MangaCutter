package net.macu.UI.imgWriter;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.macu.UI.Form;
import net.macu.UI.ViewManager;
import net.macu.UI.imgWriter.psd.PsdLevelChannelForm;
import net.macu.imgWriter.psd.ColorMode;
import net.macu.settings.History;
import net.macu.settings.L;
import net.macu.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class PsdForm implements Form {
    private final ArrayList<PsdLevelChannelForm> channels;
    private JPanel root;
    private JComboBox<ColorMode> outputColorModeComboBox;
    private JLabel outputColorModeLabel;
    private JCheckBox levelsCheckBox;
    private JLabel levelsLayerLabel;
    private JLabel layerNameLabel;
    private JTextField layerNameTextField;
    private JTabbedPane levelsTabbedPane;
    private JLabel levelsLayerNameLabel;
    private JTextField levelsLayerNameTextField;
    private JPanel levelsLayerNamePanel;

    @Override
    public boolean validateInput() {
        if (layerNameTextField.getText().isEmpty()) {
            ViewManager.showMessageDialog("UI.imgWriter.PsdForm.validateInput.layer_name_is_empty", null);
            return false;
        }
        if (levelsLayerNameTextField.getText().isEmpty()) {
            ViewManager.showMessageDialog("UI.imgWriter.PsdForm.validateInput.levels_layer_name_is_empty", null);
            return false;
        }
        return true;
    }

    @Override
    public JComponent getRootComponent() {
        return root;
    }

    private PsdLevelChannelForm addChannel(String labelId) {
        PsdLevelChannelForm form = new PsdLevelChannelForm();
        levelsTabbedPane.addTab(L.get(labelId), form.$$$getRootComponent$$$());
        return form;
    }

    @Override
    public String getName() {
        return L.get("UI.imgWriter.PsdForm.name");
    }

    public PsdForm() {
        outputColorModeLabel.setText(L.get("UI.imgWriter.PsdForm.output_color_mode_label"));
        layerNameLabel.setText(L.get("UI.imgWriter.PsdForm.layer_name_label"));
        levelsLayerLabel.setText(L.get("UI.imgWriter.PsdForm.levels_layer_label"));
        levelsLayerNameLabel.setText(L.get("UI.imgWriter.PsdForm.levels_layer_name_label"));
        outputColorModeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, L.get("imgWriter.psd.ColorMode." + ((ColorMode) value).name()), index, isSelected, cellHasFocus);
            }
        });
        levelsCheckBox.setSelected(History.getUsage("UI.imgWriter.PsdForm.levels_layer_label_true") >
                History.getUsage("UI.imgWriter.PsdForm.levels_layer_label_false"));
        channels = new ArrayList<>();
        ArrayList<ColorMode> modes = new ArrayList<>(Arrays.asList(ColorMode.values()));
        modes.sort(Comparator.comparingInt((mode) -> History.getUsage(ColorMode.class.getName() + mode.toString())));
        for (int i = 0; i < modes.size(); i++) {
            outputColorModeComboBox.insertItemAt(modes.get(i), i);
        }
        layerNameTextField.setText(Settings.PsbForm_LayerName.getValue());
        levelsLayerNameTextField.setText(Settings.PsbForm_LevelsLayerName.getValue());
        outputColorModeComboBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                levelsTabbedPane.removeAll();
                channels.clear();
                switch ((ColorMode) outputColorModeComboBox.getSelectedItem()) {
                    case RGB:
                        channels.add(addChannel("UI.imgWriter.PsdForm.master_channel_label"));
                        channels.add(addChannel("UI.imgWriter.PsdForm.red_channel_label"));
                        channels.add(addChannel("UI.imgWriter.PsdForm.green_channel_label"));
                        channels.add(addChannel("UI.imgWriter.PsdForm.blue_channel_label"));
                        break;
                    case Grayscale:
                        channels.add(addChannel("UI.imgWriter.PsdForm.master_channel_label"));
                        break;
                }
                refreshAccessibilityInTabs();
            }
        });
        outputColorModeComboBox.setSelectedIndex(0);
        levelsCheckBox.addActionListener(e -> refreshAccessibilityInTabs());
    }

    private void setAccessibility(Component comp, boolean state) {
        if (comp != null) {
            comp.setEnabled(state);
            if (comp instanceof Container) {
                for (Component component : ((Container) comp).getComponents()) {
                    setAccessibility(component, state);
                }
            }
        }
    }

    private void refreshAccessibilityInTabs() {
        boolean isEnabled = levelsCheckBox.isSelected();
        for (int i = 0; i < levelsTabbedPane.getTabCount(); i++) {
            levelsTabbedPane.setEnabledAt(i, isEnabled);
        }
        levelsTabbedPane.setEnabled(isEnabled);
        channels.forEach(form -> setAccessibility(form.$$$getRootComponent$$$(), isEnabled));
        setAccessibility(levelsLayerNamePanel, isEnabled);
    }

    @Override
    public void saveChoice() {
        History.incrementUsage(ColorMode.class.getName() + outputColorModeComboBox.getSelectedItem().toString());
        History.incrementUsage("UI.imgWriter.PsdForm.levels_layer_label_" + levelsCheckBox.isSelected());
    }

    public short getImageColorModeValue() {
        switch ((ColorMode) outputColorModeComboBox.getSelectedItem()) {
            case RGB:
                return 0x3;
            case Grayscale:
                return 0x1;
            default:
                return 0x0;
        }
    }

    public ColorMode getColorMode() {
        return (ColorMode) outputColorModeComboBox.getSelectedItem();
    }

    public boolean hasLevelsLayer() {
        return levelsCheckBox.isSelected();
    }

    public short getNumberOfChannels() {
        switch ((ColorMode) outputColorModeComboBox.getSelectedItem()) {
            case Grayscale:
                return 2;
            case RGB:
                return 4;
            default:
                return 0;
        }
    }

    public String getImageLayerName() {
        return layerNameTextField.getText();
    }

    public String getLevelsLayerName() {
        return levelsLayerNameTextField.getText();
    }

    public ArrayList<PsdLevelChannelForm> getLevels() {
        return channels;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        root = new JPanel();
        root.setLayout(new GridLayoutManager(5, 1, new Insets(3, 0, 3, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        outputColorModeComboBox = new JComboBox();
        panel1.add(outputColorModeComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        outputColorModeLabel = new JLabel();
        outputColorModeLabel.setText("Output color mode:");
        panel1.add(outputColorModeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        levelsCheckBox = new JCheckBox();
        levelsCheckBox.setSelected(false);
        levelsCheckBox.setText("");
        panel2.add(levelsCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        levelsLayerLabel = new JLabel();
        levelsLayerLabel.setText("Add levels layer");
        panel2.add(levelsLayerLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        layerNameLabel = new JLabel();
        layerNameLabel.setText("Layer name:");
        panel3.add(layerNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        layerNameTextField = new JTextField();
        panel3.add(layerNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        levelsLayerNamePanel = new JPanel();
        levelsLayerNamePanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        root.add(levelsLayerNamePanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        levelsLayerNameLabel = new JLabel();
        levelsLayerNameLabel.setText("Levels layer name:");
        levelsLayerNamePanel.add(levelsLayerNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        levelsLayerNameTextField = new JTextField();
        levelsLayerNamePanel.add(levelsLayerNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        levelsTabbedPane = new JTabbedPane();
        levelsTabbedPane.setEnabled(true);
        levelsTabbedPane.setTabLayoutPolicy(1);
        levelsTabbedPane.setTabPlacement(1);
        root.add(levelsTabbedPane, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(0, 0), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }

}
