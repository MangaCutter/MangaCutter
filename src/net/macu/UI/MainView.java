package net.macu.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.macu.browser.plugin.BrowserPlugin;
import net.macu.browser.proxy.cert.CertificateAuthority;
import net.macu.core.IOManager;
import net.macu.core.JobManager;
import net.macu.core.Main;
import net.macu.service.ServiceManager;
import net.macu.settings.History;
import net.macu.settings.L;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class MainView {
    private JTextField urlTextField;
    private JButton startButton;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JPanel mainPanel;
    private JComboBox<String> pathSelector;
    private final JobManager jobManager = new JobManager();
    private JLabel urlLabel;
    private Form currentForm;
    private final ViewManager viewManager;
    private JPanel selectableFormPanel;
    private JButton clearButton;
    private BufferedImage[] fragments;
    private final JFrame frame;

    private MainView(boolean prepared) {
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
                    if (!prepared) {
                        if (!BrowserPlugin.getPlugin().hasActiveRequests() || ViewManager.showConfirmDialog("UI.MainView.confirm_exit", frame)) {
                            System.exit(0);
                        }
                    } else {
                        frame.dispose();
                    }
                }).start();
            }
        });
        frame.setContentPane($$$getRootComponent$$$());
        urlLabel.setText(L.get("UI.MainView.url_label"));
        cancelButton.setText(L.get("UI.MainView.cancel_button"));
        startButton.setText(L.get("UI.MainView.start_button"));
        clearButton.setIcon(IconManager.getClearIcon());
        if (prepared) urlTextField.setEnabled(false);

        viewManager = new ViewManager(this);

        ArrayList<Form> local = new ArrayList<>(Main.getForms());
        local.sort(Comparator.comparing((form) -> -History.getUsage(form.getClass().getName())));
        Iterator<Form> forms = local.iterator();
        for (int i = 0; forms.hasNext(); i++) {
            pathSelector.insertItemAt(forms.next().getDescription(), i);
        }
        if (!prepared) {
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

            JMenu pluginMenu = new JMenu(L.get("UI.ViewManager.plugin_menu"));

            JMenuItem generateCertificateItem =
                    new JMenuItem(L.get("UI.ViewManager.generate_certificate_menu"));
            generateCertificateItem.addActionListener(e -> new Thread(CertificateAuthority::openGenerateCertFrame).start());
            pluginMenu.add(generateCertificateItem);
            JMenuItem pluginConnectionItem = new JMenuItem(L.get("UI.ViewManager.plugin_connection_menu"));
            pluginConnectionItem.addActionListener(e -> new Thread(() ->
                    ViewManager.showMessageDialogForced("UI.ViewManager.plugin_connection", frame,
                            BrowserPlugin.getPlugin().isConnected() ?
                                    L.get("UI.ViewManager.plugin_connection_true") :
                                    L.get("UI.ViewManager.plugin_connection_false"))).start());
            pluginMenu.add(pluginConnectionItem);

            JMenuItem exportCertificateItem = new JMenuItem(L.get("UI.ViewManager.certificate_export_menu"));
            exportCertificateItem.addActionListener(e ->
                    new Thread(CertificateAuthority::openExportCertificateFrame).start());
            pluginMenu.add(exportCertificateItem);

            bar.add(pluginMenu);

            frame.setJMenuBar(bar);
        }

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
                History.incrementUsage(local.get(pathSelector.getSelectedIndex()).getClass().getName());
                if (validateInput()) {
                    if (prepared) {
                        if (jobManager.runJob(fragments, currentForm.getConfiguredPipeline(), viewManager)) {
                            ViewManager.showMessageDialog("UI.MainView.complete_message", frame);
                            frame.dispose();
                            return;
                        }
                    } else {
                        if (jobManager.runJob(urlTextField.getText(), currentForm.getConfiguredPipeline(), viewManager)) {
                            ViewManager.showMessageDialog("UI.MainView.complete_message", frame);
                        }
                    }
                }
                viewManager.resetProgress();
                cancelButton.setEnabled(false);
                startButton.setEnabled(true);
            });
            t.start();
        });
        clearButton.addActionListener(e -> new Thread(() -> {
            urlTextField.setText("");
            urlTextField.requestFocusInWindow();
        }).start());
        pathSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                currentForm = local.get(pathSelector.getSelectedIndex());
                selectableFormPanel.removeAll();
                selectableFormPanel.add(currentForm.getRootComponent());
                frame.pack();
            }
        });

        pathSelector.setSelectedIndex(0);

        frame.setVisible(true);
    }

    public MainView() {
        this(false);
    }

    public MainView(BufferedImage[] fragments, String url) {
        this(true);
        this.fragments = fragments;
        urlTextField.setText(url);
    }

    public boolean validateInput() {
        if (urlTextField.getText().isEmpty()) {
            ViewManager.showMessageDialog("UI.MainView.validateInput.empty_url", frame);
            return false;
        }
        return currentForm.validateInput();
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
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(10, 10, 3, 10), -1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
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
        mainPanel.add(panel2, gbc);
        pathSelector = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 10, 3, 10);
        panel2.add(pathSelector, gbc);
        selectableFormPanel = new JPanel();
        selectableFormPanel.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 10, 3, 10);
        mainPanel.add(selectableFormPanel, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(3, 10, 3, 10), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(panel3, gbc);
        progressBar = new JProgressBar();
        progressBar.setEnabled(false);
        progressBar.setString("0%");
        progressBar.setStringPainted(true);
        panel3.add(progressBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(3, 10, 10, 10), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
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
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    public JFrame getFrame() {
        return frame;
    }
}
