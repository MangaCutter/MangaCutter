package net.ddns.masterlogick.UI;

import net.ddns.masterlogick.core.JobManager;
import net.ddns.masterlogick.service.ServiceManager;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class ViewManager {
    private static JFrame frame;
    private static MainView view;
    private static JFileChooser singleFileChooser;
    private static JFileChooser multipleFileChooser;
    private static JFileChooser asIsFileChooser;
    public static final int MULTI_INDEX = 0;
    public static final int SINGLE_INDEX = 1;
    public static final int AS_IS_INDEX = 2;

    public static void startProgress(int max, String message) {
        view.progressBar.setValue(0);
        view.progressBar.setMaximum(max);
        view.progressBar.setString(message);
        view.progressBar.setEnabled(true);
    }

    public static void incrementProgress(String message) {
        view.progressBar.setValue(view.progressBar.getValue() + 1);
        view.progressBar.setString(message);
    }

    public static void resetProgress() {
        view.progressBar.setValue(0);
        view.progressBar.setMaximum(1);
        view.progressBar.setString("");
        view.progressBar.setEnabled(false);
    }

    public static void showMessage(String s) {
        JOptionPane.showMessageDialog(frame, s);
    }

    public static void createView() {
        constructSingleFileChooser();
        multipleFileChooser = constructDirChooser();
        asIsFileChooser = constructDirChooser();
        constructFrame();
        frame.setJMenuBar(constructMenu());
        view = new MainView();
        configureComboBox();
        frame.setContentPane(view.mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void showSingleFileChooser() {
        if (singleFileChooser.showDialog(frame, "Сохранить") == JFileChooser.APPROVE_OPTION) {
            String path = singleFileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png"))
                path += ".png";
            if (new File(path).exists()) {
                if (JOptionPane.showConfirmDialog(frame, "Выбранный файл уже существует\nПерезаписать?", "Внимание!", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            view.singleFileTextField.setText(path);
        }
    }

    public static void showMultipleFileChooser() {
        if (multipleFileChooser.showDialog(frame, "Сохранить") == JFileChooser.APPROVE_OPTION) {
            view.multiDirTextField.setText(multipleFileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    public static void showAsIsFileChooser() {
        if (asIsFileChooser.showDialog(frame, "Сохранить") == JFileChooser.APPROVE_OPTION) {
            view.asIsDirTextField.setText(asIsFileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private static void constructFrame() {
        if (frame == null) {
            frame = new JFrame("MangaCutter");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    JobManager.cancel();
                    frame.dispose();
                    System.exit(0);
                }
            });
        }
    }

    private static void constructSingleFileChooser() {
        singleFileChooser = new JFileChooser();
        singleFileChooser.setDialogTitle("Куда сохранить?");
        singleFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        singleFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        singleFileChooser.setAcceptAllFileFilterUsed(false);
        singleFileChooser.setMultiSelectionEnabled(false);
        singleFileChooser.setFileSystemView(FileSystemView.getFileSystemView());
        singleFileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG file";
            }
        });
    }

    private static JFileChooser constructDirChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Куда сохранить?");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSystemView(FileSystemView.getFileSystemView());
        return fileChooser;
    }

    private static JMenuBar constructMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Справка");

        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(actionEvent -> ViewManager.showMessage("MangaCutter\n" +
                "Программа для скачивания и склейки сканов с корейского вебтуна\nАвтор: MasterLogick\n" +
                "https://github.com/MasterLogick/MangaCutter"));

        JMenuItem supportedServicesItem = new JMenuItem("Поддерживаемые сервисы");
        supportedServicesItem.addActionListener(actionEvent -> ViewManager.showMessage("Список поддерживаемых сервисов:\n" + ServiceManager.getSupportedServicesList()));

        menu.add(aboutItem);
        menu.add(supportedServicesItem);
        bar.add(menu);
        return bar;
    }

    private static void configureComboBox() {
        final String pastaVariant = "Разрезать главу на несколько коротких кусков";
        final String singlePageVariant = "Склеить всё в один длинный скан";
        final String asIsVariant = "Сохранить сканы \"как есть\"";
        view.pathSelector.insertItemAt(pastaVariant, MULTI_INDEX);
        view.pathSelector.insertItemAt(singlePageVariant, SINGLE_INDEX);
        view.pathSelector.insertItemAt(asIsVariant, AS_IS_INDEX);
        view.pathSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                switch (view.pathSelector.getSelectedIndex()) {
                    case SINGLE_INDEX:
                        view.singlePagePanel.setVisible(true);
                        view.multiPagePanel.setVisible(false);
                        view.asIsPanel.setVisible(false);
                        break;
                    case MULTI_INDEX:
                        view.multiPagePanel.setVisible(true);
                        view.singlePagePanel.setVisible(false);
                        view.asIsPanel.setVisible(false);
                        break;
                    case AS_IS_INDEX:
                        view.asIsPanel.setVisible(true);
                        view.multiPagePanel.setVisible(false);
                        view.singlePagePanel.setVisible(false);
                        break;
                }
                frame.pack();
            }
        });
        view.pathSelector.setSelectedIndex(0);
    }
}
