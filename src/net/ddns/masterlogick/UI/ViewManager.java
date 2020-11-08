package net.ddns.masterlogick.UI;

import net.ddns.masterlogick.core.JobManager;
import net.ddns.masterlogick.service.ServiceManager;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class ViewManager {
    private static JFrame frame;
    private static MainView view;
    private static JFileChooser fileChooser;

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
        constructFileChooser();
        constructFrame();
        frame.setJMenuBar(constructMenu());
        view = new MainView();
        frame.setContentPane(view.mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void showFileChooser() {
        if (fileChooser.showDialog(frame, "Сохранить") == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png"))
                path += ".png";
            if (new File(path).exists()) {
                if (JOptionPane.showConfirmDialog(frame, "Выбранный файл уже существует\nПерезаписать?", "Внимание!", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            view.fileTextField.setText(path);
        }
    }

    private static void constructFrame() {
        if (frame == null) {
            frame = new JFrame("Webtoon Downloader");
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

    private static void constructFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Куда сохранить?");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSystemView(FileSystemView.getFileSystemView());
        fileChooser.addChoosableFileFilter(new FileFilter() {
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

    private static JMenuBar constructMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Справка");

        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(actionEvent -> ViewManager.showMessage("WebtoonDownloader\n" +
                "Программа для скачивания и склейки сканов с корейского вебтуна\nАвтор: MasterLogick\n" +
                "https://github.com/MasterLogick/WebtoonDownloader"));

        JMenuItem supportedServicesItem = new JMenuItem("Поддерживаемые сервисы");
        supportedServicesItem.addActionListener(actionEvent -> ViewManager.showMessage("Список поддерживаемых сервисов:\n" + ServiceManager.getSupportedServicesList()));

        menu.add(aboutItem);
        menu.add(supportedServicesItem);
        bar.add(menu);
        return bar;
    }
}
