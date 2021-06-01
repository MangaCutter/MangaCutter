package net.macu.UI.psd;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import net.macu.UI.ViewManager;
import net.macu.settings.L;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PsdLevelChannelForm {
    private JPanel root;
    private JLabel inputRangeLabel;
    private JTextField inputRangeFloorTextField;
    private JTextField inputRangeCeilingTextField;
    private JTextField outputRangeFloorTextField;
    private JTextField outputRangeCeilingTextField;
    private JLabel gammaLabel;
    private JTextField gammaTextField;
    private JLabel outputRangeLabel;
    private int inputRangeFloor;
    private int inputRangeCeiling;
    private int outputRangeFloor;
    private int outputRangeCeiling;
    private int gamma;

    public PsdLevelChannelForm() {
        $$$setupUI$$$();
        inputRangeLabel.setText(L.get("UI.psd.PsdLevelChannelForm.input_range_label"));
        outputRangeLabel.setText(L.get("UI.psd.PsdLevelChannelForm.output_range_label"));
        gammaLabel.setText(L.get("UI.psd.PsdLevelChannelForm.gamma_label"));
        inputRangeFloor = 0;
        inputRangeCeiling = 255;
        outputRangeCeiling = 255;
        outputRangeFloor = 0;
        gamma = 100;
        inputRangeFloorTextField.setText(String.valueOf(inputRangeFloor));
        inputRangeCeilingTextField.setText(String.valueOf(inputRangeCeiling));
        outputRangeFloorTextField.setText(String.valueOf(outputRangeFloor));
        outputRangeCeilingTextField.setText(String.valueOf(outputRangeCeiling));
        setGammaTextField(gamma);
        inputRangeFloorTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                new Thread(() -> {
                    String text = inputRangeFloorTextField.getText();
                    try {
                        int val = Integer.parseInt(text);
                        if (val >= 0 && val <= 255) {
                            inputRangeFloor = val;
                            return;
                        } else {
                            ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.bad_range", null);
                        }
                    } catch (NumberFormatException ex) {
                        ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.nan", null);
                    }
                    inputRangeFloorTextField.setText(String.valueOf(inputRangeFloor));
                }).start();
            }
        });
        inputRangeCeilingTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                new Thread(() -> {
                    String text = inputRangeCeilingTextField.getText();
                    try {
                        int val = Integer.parseInt(text);
                        if (val >= 0 && val <= 255) {
                            inputRangeCeiling = val;
                            return;
                        } else {
                            ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.bad_range", null);
                        }
                    } catch (NumberFormatException ex) {
                        ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.nan", null);
                    }
                    inputRangeCeilingTextField.setText(String.valueOf(inputRangeCeiling));
                }).start();
            }
        });
        outputRangeFloorTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                new Thread(() -> {
                    String text = outputRangeFloorTextField.getText();
                    try {
                        int val = Integer.parseInt(text);
                        if (val >= 0 && val <= 255) {
                            outputRangeFloor = val;
                            return;
                        } else {
                            ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.bad_range", null);
                        }
                    } catch (NumberFormatException ex) {
                        ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.nan", null);
                    }
                    outputRangeFloorTextField.setText(String.valueOf(outputRangeFloor));
                }).start();
            }
        });
        outputRangeCeilingTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                new Thread(() -> {
                    String text = outputRangeCeilingTextField.getText();
                    try {
                        int val = Integer.parseInt(text);
                        if (val >= 0 && val <= 255) {
                            outputRangeCeiling = val;
                            return;
                        } else {
                            ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.bad_range", null);
                        }
                    } catch (NumberFormatException ex) {
                        ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.nan", null);
                    }
                    outputRangeCeilingTextField.setText(String.valueOf(outputRangeCeiling));
                }).start();
            }
        });
        gammaTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                new Thread(() -> {
                    String[] text = gammaTextField.getText().split("\\.");
                    if (text.length != 2) {
                        ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.invalid_gamma", null);
                    } else {
                        try {
                            int numerator = Integer.parseInt(text[0]);
                            int denominator = Integer.parseInt(text[1]);
                            if (numerator >= 0 && numerator <= 9 && denominator >= 0 && denominator <= 99 && numerator + denominator != 0) {
                                gamma = numerator * 100 + denominator;
                                return;
                            } else {
                                ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.bad_range", null);
                            }
                        } catch (NumberFormatException ex) {
                            ViewManager.showMessageDialog("UI.psd.PsdLevelChannelForm.nan", null);
                        }
                    }
                    setGammaTextField(gamma);
                }).start();
            }
        });
    }

    private void setGammaTextField(int gamma) {
        StringBuilder sb = new StringBuilder(String.valueOf(gamma));
        while (sb.length() < 3) {
            sb.append('0');
        }
        gammaTextField.setText(sb.insert(1, '.').toString());
    }

    private void createUIComponents() {
        inputRangeFloorTextField = new JTextField(3);
        inputRangeCeilingTextField = new JTextField(3);
        outputRangeFloorTextField = new JTextField(3);
        outputRangeCeilingTextField = new JTextField(3);
        gammaTextField = new JTextField(3);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        root = new JPanel();
        root.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        root.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        inputRangeLabel = new JLabel();
        inputRangeLabel.setText("Input range:");
        panel1.add(inputRangeLabel);
        inputRangeFloorTextField.setColumns(3);
        panel1.add(inputRangeFloorTextField);
        final JLabel label1 = new JLabel();
        label1.setText("-");
        panel1.add(label1);
        panel1.add(inputRangeCeilingTextField);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        root.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        outputRangeLabel = new JLabel();
        outputRangeLabel.setText("Output range:");
        panel2.add(outputRangeLabel);
        panel2.add(outputRangeFloorTextField);
        final JLabel label2 = new JLabel();
        label2.setText("-");
        panel2.add(label2);
        panel2.add(outputRangeCeilingTextField);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        root.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        gammaLabel = new JLabel();
        gammaLabel.setText("Gamma:");
        panel3.add(gammaLabel);
        panel3.add(gammaTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }

}
