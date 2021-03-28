package net.macu.UI;

import net.macu.settings.History;
import net.macu.settings.L;
import net.macu.settings.Settings;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class ViewManager {
    private static IconifiedFileChooser singleFileChooser;
    private static IconifiedFileChooser dirChooser;
    private final MainView mainView;
    public static final String[] SUPPORTED_THEMES = new String[]{"FlatDarkLaf", "FlatLightLaf"};

    public ViewManager(MainView mainView) {
        this.mainView = mainView;
    }

    public static void showMessageDialog(String id, Component parent, Object... args) {
        if (History.userAllowedToShowNotification(id)) {
            showMessageDialog(parent, L.get(id, args), id, false);
        } else {
            System.out.println("Skipped dialog " + id);
        }
    }

    public static void showMessageDialogForced(String id, Component parent, Object... args) {
        showMessageDialog(parent, L.get(id, args), id, true);
    }

    private static void showMessageDialog(Component parent, String s, String id, boolean forced) {
        //in case if I forgot to process event in separate thread
        if (Thread.currentThread().getName().startsWith("AWT-EventQueue")) {
            JOptionPane.showMessageDialog(parent, Arrays.toString(Thread.currentThread().getStackTrace()));
            return;
        }

        Object locker = new Object();
        if (!s.startsWith("<html>")) {
            s = "<html>" + s.replaceAll("\n", "<br>") + "</html>";
        }
        JEditorPane editorPane1 = new JEditorPane("text/html", s);
        editorPane1.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        editorPane1.setEditable(false);
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        JCheckBox dontShowAgainCheckBox = null;
        if (!forced) {
            root.add(editorPane1);
            JPanel cPanel = new JPanel();
            cPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            dontShowAgainCheckBox = new JCheckBox(L.get("UI.ViewManager.showMessageDialog.dont_show_again"), false);
            cPanel.add(dontShowAgainCheckBox);
            root.add(cPanel);
        } else root.add(editorPane1);
        JOptionPane pane = new JOptionPane(root, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, null, null);
        pane.addPropertyChangeListener(evt -> {
            synchronized (locker) {
                locker.notify();
            }
        });
        editorPane1.setBackground(pane.getBackground());
        pane.selectInitialValue();
        JFrame f = History.createJFrameFromHistory("UI.ViewManager.message_dialog_title", 100, 100);
        f.setTitle(L.get("UI.ViewManager.message_dialog_title"));
        f.setIconImage(IconManager.getBrandImage());
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setResizable(false);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (locker) {
                    locker.notify();
                }
            }
        });
        f.setContentPane(pane);
        f.pack();
        f.setVisible(true);
        synchronized (locker) {
            try {
                locker.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        f.dispose();
        if (!forced && dontShowAgainCheckBox.isSelected()) {
            History.disallowToShowNotification(id);
        }
    }

    public static boolean showConfirmDialog(String id, Component parent, Object... args) {
        if (History.userAllowedToShowNotification(id)) {
            return showConfirmDialog(parent, L.get(id, args), id, false);
        } else {
            System.out.println("Skipped dialog " + id + " " + History.getSavedAnswer(id));
            return History.getSavedAnswer(id);
        }
    }

    public static boolean showConfirmDialogForced(String id, Component parent, Object... args) {
        return showConfirmDialog(parent, L.get(id, args), id, true);
    }

    private static boolean showConfirmDialog(Component parent, String s, String id, boolean forced) {
        //in case if I forgot to process event in separate thread
        if (Thread.currentThread().getName().startsWith("AWT-EventQueue")) {
            JOptionPane.showMessageDialog(parent, Arrays.toString(Thread.currentThread().getStackTrace()));
            return false;
        }

        Object locker = new Object();
        if (!s.startsWith("<html>")) {
            s = "<html>" + s.replaceAll("\n", "<br>") + "</html>";
        }
        JEditorPane editorPane1 = new JEditorPane("text/html", s);
        editorPane1.setBackground(new JLabel().getBackground());
        editorPane1.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        editorPane1.setEditable(false);
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        JCheckBox dontShowAgainCheckBox = null;
        if (!forced) {
            root.add(editorPane1);
            JPanel cPanel = new JPanel();
            cPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            dontShowAgainCheckBox = new JCheckBox(L.get("UI.ViewManager.showConfirmDialog.dont_show_again"), false);
            cPanel.add(dontShowAgainCheckBox);
            root.add(cPanel);
        } else root.add(editorPane1);
        JOptionPane pane = new JOptionPane(root, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, null, null);
        pane.addPropertyChangeListener(evt -> {
            synchronized (locker) {
                locker.notify();
            }
        });
        editorPane1.setBackground(pane.getBackground());
        pane.selectInitialValue();
        JFrame f = History.createJFrameFromHistory("UI.ViewManager.confirm_dialog_title", 100, 100);
        f.setTitle(L.get("UI.ViewManager.confirm_dialog_title"));
        f.setIconImage(IconManager.getBrandImage());
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setResizable(false);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (locker) {
                    locker.notify();
                }
            }
        });
        f.setContentPane(pane);
        f.pack();
        f.setVisible(true);
        synchronized (locker) {
            try {
                locker.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        f.dispose();

        Object selectedValue = pane.getValue();
        boolean answer;
        if (selectedValue == null) {
            answer = false;
        } else if (selectedValue instanceof Integer) {
            answer = (Integer) selectedValue == JOptionPane.OK_OPTION;
        } else {
            answer = false;
        }
        if (!forced && dontShowAgainCheckBox.isSelected()) {
            History.disallowToShowNotification(id);
            History.saveAnswer(id, answer);
        }
        return answer;
    }

    public static String requestChooseSingleFile(String extension, Component parent) {
        String path = null;
        singleFileChooser.resetChoosableFileFilters();
        singleFileChooser.addChoosableFileFilter(new FileFilterImpl(extension));
        if (singleFileChooser.showDialog(parent, L.get("UI.ViewManager.single_file_approve_button")) == JFileChooser.APPROVE_OPTION) {
            path = singleFileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith("." + extension)) {
                path += "." + extension;
            }
        }
        return path;
    }

    public static String requestChooseDir(Component parent) {
        if (dirChooser.showDialog(parent, L.get("UI.ViewManager.dir_select_button")) == JFileChooser.APPROVE_OPTION) {
            return dirChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public static void showPreviewFrame(BufferedImage image, Component parent) {
        if (image != null) {
            JFrame f = History.createJFrameFromHistory("UI.ViewManager.preview_frame_title", 600, 600);
            f.setTitle(L.get("UI.ViewManager.preview_frame_title"));
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(image)));
            pane.getVerticalScrollBar().setUnitIncrement(Settings.ViewManager_MasterScrollSpeed.getValue());
            pane.getHorizontalScrollBar().setUnitIncrement(Settings.ViewManager_MasterScrollSpeed.getValue());
            f.add(pane);
            f.setIconImage(IconManager.getBrandImage());
            f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    f.dispose();
                }
            });
            f.setVisible(true);
        }
    }

    public static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf." + Settings.ViewManager_LookAndFeel.getValue());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void initFileChoosers() {
        singleFileChooser = History.createIconifiedFileChooserFromHistory("UI.ViewManager.file_chooser_title");
        singleFileChooser.setIcon(IconManager.getBrandImage());
        singleFileChooser.setDialogTitle(L.get("UI.ViewManager.file_chooser_title"));
        singleFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        singleFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        singleFileChooser.setAcceptAllFileFilterUsed(false);
        singleFileChooser.setMultiSelectionEnabled(false);
        dirChooser = History.createIconifiedFileChooserFromHistory("UI.ViewManager.dir_chooser_title");
        dirChooser.setIcon(IconManager.getBrandImage());
        dirChooser.setDialogTitle(L.get("UI.ViewManager.dir_chooser_title"));
        dirChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setAcceptAllFileFilterUsed(false);
        dirChooser.setMultiSelectionEnabled(false);
    }

    public void startProgress(int max, String message) {
        mainView.startProgress(max, message);
    }

    public void incrementProgress(String message) {
        mainView.incrementProgress(message);
    }

    public void resetProgress() {
        mainView.resetProgress();
    }

    public JFrame getView() {
        return mainView.getFrame();
    }
}
