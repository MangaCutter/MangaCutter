package net.macu.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.macu.core.IOManager;
import net.macu.core.JobManager;
import net.macu.core.Main;
import net.macu.cutter.Cutter;
import net.macu.imgWriter.ImgWriter;
import net.macu.service.ServiceManager;
import net.macu.settings.History;
import net.macu.settings.L;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class MainView {
    private JTextField urlTextField;
    private JButton startButton;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JPanel mainPanel;
    private final ActionListener filepathButtonDirSelectorListener;
    private final JobManager jobManager = new JobManager();
    private JLabel urlLabel;
    private final ViewManager viewManager;
    private final FormContainer cutterFormContainer;
    private JButton clearButton;
    private final ActionListener filepathButtonFileSelectorListener;
    private final FormContainer imgWriterFormContainer;
    private JTextField filepathTextField;
    private final JFrame frame;
    private JButton filepathButton;
    private JLabel filepathLabel;
    private JLabel imageFormatLabel;
    private JPanel cutterFormPanel;
    private JComboBox<Cutter> cutterSelector;
    private JComboBox<ImgWriter> imgWriterSelector;
    private JPanel imgWriterFormPanel;

    public MainView() {
        frame = History.createJFrameFromHistory("UI.ViewManager.main_frame_title", 0, 0);
        frame.setTitle(L.get("UI.ViewManager.main_frame_title"));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setIconImage(IconManager.getBrandImage());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    jobManager.cancel();
                    frame.dispose();
                    System.exit(0);
                }).start();
            }
        });
        frame.setContentPane($$$getRootComponent$$$());
        urlLabel.setText(L.get("UI.MainView.url_label"));
        cancelButton.setText(L.get("UI.MainView.cancel_button"));
        startButton.setText(L.get("UI.MainView.start_button"));
        filepathButton.setText(L.get("UI.MainView.filepath_button"));
        clearButton.setIcon(IconManager.getClearIcon());
        imageFormatLabel.setText(L.get("UI.MainView.imageformat_label"));

        cutterFormContainer = new FormContainer();
        cutterFormPanel.add(cutterFormContainer);
        imgWriterFormContainer = new FormContainer();
        imgWriterFormPanel.add(imgWriterFormContainer);

        viewManager = new ViewManager(this);

        ArrayList<Cutter> cutters = new ArrayList<>(Main.getCutters());
        cutters.sort(Comparator.comparing((cutter) -> -History.getUsage(cutter.getClass().getName())));
        Iterator<Cutter> cutterIterator = cutters.iterator();
        for (int i = 0; cutterIterator.hasNext(); i++) {
            cutterSelector.insertItemAt(cutterIterator.next(), i);
        }
        ArrayList<ImgWriter> imgWriters = new ArrayList<>(Main.getImgWriters());
        imgWriters.sort(Comparator.comparing((imgWriter) -> -History.getUsage(imgWriter.getClass().getName())));
        Iterator<ImgWriter> imgWriterIterator = imgWriters.iterator();
        for (int i = 0; imgWriterIterator.hasNext(); i++) {
            imgWriterSelector.insertItemAt(imgWriterIterator.next(), i);
        }
        setupMenuBar();

        clearButton.addActionListener(e -> new Thread(() -> {
            urlTextField.setText("");
            urlTextField.requestFocusInWindow();
        }).start());

        urlTextField.setTransferHandler(new FileTransferHandler(urlTextField));
        urlTextField.setToolTipText(L.get("UI.MainView.urlTextField.tooltip"));

        cancelButton.addActionListener(e -> {
            Thread t = new Thread(() -> {
                jobManager.cancel();
                viewManager.resetProgress();
                startButton.setEnabled(true);
                cancelButton.setEnabled(false);
            });
            t.start();
        });
        startButton.addActionListener(e -> {
            Thread t = new Thread(() -> {
                startButton.setEnabled(false);
                cancelButton.setEnabled(true);
                try {
                    if (validateInput()) {
                        History.incrementUsage(((Cutter) cutterSelector.getSelectedItem()).getClass().getName());
                        History.incrementUsage(((ImgWriter) imgWriterSelector.getSelectedItem()).getClass().getName());
                        imgWriterFormContainer.saveChoice();
                        cutterFormContainer.saveChoice();
                        if (jobManager.runJob(urlTextField.getText().trim(), (Cutter) cutterSelector.getSelectedItem(),
                                filepathTextField.getText(), (ImgWriter) imgWriterSelector.getSelectedItem(),
                                viewManager)) {
                            ViewManager.showMessageDialog("UI.MainView.complete_message", frame);
                        }
                    }
                    viewManager.resetProgress();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                cancelButton.setEnabled(false);
                startButton.setEnabled(true);
            });
            t.start();
        });

        filepathButtonFileSelectorListener = e -> {
            String path = ViewManager.requestChooseSingleFile(
                    ((ImgWriter) imgWriterSelector.getSelectedItem()).getExtension(), frame);
            if (path != null) {
                filepathTextField.setText(path);
            }
        };
        filepathButtonDirSelectorListener = e -> {
            String path = ViewManager.requestChooseDir(frame);
            if (path != null) {
                filepathTextField.setText(path);
            }
        };

        cutterSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((Cutter) value).getDescription(), index, isSelected, cellHasFocus);
            }
        });
        cutterSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Cutter selectedCutter = (Cutter) cutterSelector.getSelectedItem();
                cutterFormContainer.setForm(selectedCutter.getOptionsForm());
                frame.pack();
                for (ActionListener actionListener : filepathButton.getActionListeners()) {
                    filepathButton.removeActionListener(actionListener);
                }
                filepathTextField.setText("");
                if (selectedCutter.isReturnsSingleFile()) {
                    filepathLabel.setText(L.get("UI.MainView.filepath_save_as_label"));
                    filepathButton.addActionListener(filepathButtonFileSelectorListener);
                } else {
                    filepathLabel.setText(L.get("UI.MainView.filepath_save_in_label"));
                    filepathButton.addActionListener(filepathButtonDirSelectorListener);
                }
            }
        });
        cutterSelector.setSelectedIndex(0);

        imgWriterSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((ImgWriter) value).getDescription(), index, isSelected, cellHasFocus);
            }
        });
        imgWriterSelector.addItemListener((e) -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        if (!filepathTextField.getText().isEmpty() &&
                                ((Cutter) cutterSelector.getSelectedItem()).isReturnsSingleFile()) {
                            String oldPath = filepathTextField.getText();
                            if (oldPath.lastIndexOf('.') != -1) {
                                filepathTextField.setText(oldPath.substring(0, oldPath.lastIndexOf(".")) + "." +
                                        ((ImgWriter) imgWriterSelector.getSelectedItem()).getExtension().toLowerCase());
                            } else {
                                filepathTextField.setText(oldPath + "." +
                                        ((ImgWriter) imgWriterSelector.getSelectedItem()).getExtension().toLowerCase());
                            }
                        }
                        imgWriterFormContainer.setForm(((ImgWriter) imgWriterSelector.getSelectedItem()).getOptionsForm());
                        frame.pack();
                    }
                }
        );
        imgWriterSelector.setSelectedIndex(0);
        frame.pack();
        frame.setVisible(true);
    }

    private void setupMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu(L.get("UI.ViewManager.help_menu"));
        JMenuItem settingsMenu = new JMenuItem(L.get("UI.ViewManager.settings_menu"));
        settingsMenu.addActionListener(e -> new Thread(SettingsFrame::openFrame).start());
        fileMenu.add(settingsMenu);
        JMenuItem supportedServicesItem = new JMenuItem(L.get("UI.ViewManager.supported_services_menu"));
        supportedServicesItem.addActionListener(actionEvent -> new Thread(() ->
                ViewManager.showMessageDialogForced("UI.ViewManager.supported_services_list", frame,
                        ServiceManager.getSupportedServicesList())).start());
        fileMenu.add(supportedServicesItem);

        JMenuItem checkUpdateItem = new JMenuItem(L.get("UI.ViewManager.check_updates"));
        checkUpdateItem.addActionListener((e) -> new Thread(() -> {
            if (IOManager.checkUpdates(true))
                ViewManager.showMessageDialogForced("UI.ViewManager.up_to_date", frame);
        }).start());
        fileMenu.add(checkUpdateItem);

        JMenuItem aboutItem = new JMenuItem(L.get("UI.ViewManager.about_menu"));
        aboutItem.addActionListener(actionEvent -> new Thread(() -> ViewManager.showMessageDialogForced("UI.ViewManager.about_text", frame, Main.getVersion())).start());
        fileMenu.add(aboutItem);

        bar.add(fileMenu);

        frame.setJMenuBar(bar);
    }

    public boolean validateInput() {
        if (urlTextField.getText().isEmpty()) {
            ViewManager.showMessageDialog("UI.MainView.validateInput.empty_url", frame);
            return false;
        }
        if (filepathTextField.getText().isEmpty()) {
            ViewManager.showMessageDialog("UI.MainView.validateInput.empty_path", null);
            return false;
        }
        return cutterFormContainer.validateInput() && imgWriterFormContainer.validateInput();
    }

    public void startProgress(int max, String message) {
        progressBar.setValue(0);
        progressBar.setMaximum(max);
        progressBar.setString(message);
        progressBar.setEnabled(true);
    }

    public void incrementProgress(String message) {
        progressBar.setValue(progressBar.getValue() + 1);
        progressBar.setString(message);
    }

    public void resetProgress() {
        progressBar.setValue(0);
        progressBar.setMaximum(1);
        progressBar.setString("");
        progressBar.setEnabled(false);
    }

    public JFrame getFrame() {
        return frame;
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 3, 10);
        mainPanel.add(panel1, gbc);
        urlLabel = new JLabel();
        urlLabel.setText("Link to chapter:");
        panel1.add(urlLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        urlTextField = new JTextField();
        panel1.add(urlTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        clearButton = new JButton();
        panel1.add(clearButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 10, 3, 10);
        mainPanel.add(panel2, gbc);
        cutterSelector = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(cutterSelector, gbc);
        cutterFormPanel = new JPanel();
        cutterFormPanel.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);
        mainPanel.add(cutterFormPanel, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 10, 3, 10);
        mainPanel.add(panel3, gbc);
        progressBar = new JProgressBar();
        progressBar.setEnabled(false);
        progressBar.setString("0%");
        progressBar.setStringPainted(true);
        panel3.add(progressBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 10, 10, 10);
        mainPanel.add(panel4, gbc);
        startButton = new JButton();
        startButton.setText("Start");
        panel4.add(startButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setEnabled(false);
        cancelButton.setText("Cancel");
        panel4.add(cancelButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 10, 3, 10);
        mainPanel.add(panel5, gbc);
        filepathLabel = new JLabel();
        filepathLabel.setText("Save in:");
        panel5.add(filepathLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filepathTextField = new JTextField();
        panel5.add(filepathTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        filepathButton = new JButton();
        filepathButton.setText("Browse");
        panel5.add(filepathButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 10, 3, 10);
        mainPanel.add(panel6, gbc);
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        imgWriterSelector = new JComboBox();
        panel6.add(imgWriterSelector, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        imageFormatLabel = new JLabel();
        imageFormatLabel.setText("Image format:");
        panel6.add(imageFormatLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        imgWriterFormPanel = new JPanel();
        imgWriterFormPanel.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);
        mainPanel.add(imgWriterFormPanel, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
