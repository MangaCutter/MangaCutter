package net.macu.core;

import net.macu.UI.IconManager;
import net.macu.settings.L;
import org.apache.http.HttpEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SelfUpdater {
    public static void selfUpdate(String url) throws URISyntaxException, IOException {
        final boolean[] interrupt = {false};
        HttpEntity entity = IOManager.sendRawRequest(url);
        InputStream in = entity.getContent();
        JFrame f = new JFrame(L.get("core.SelfUpdater.frame_title"));
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setIconImage(IconManager.getBrandIcon());
        f.setResizable(false);
        f.setLocationRelativeTo(null);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                interrupt[0] = true;
                f.dispose();
            }
        });
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JProgressBar progress = new JProgressBar(0, (int) entity.getContentLength());
        progress.setStringPainted(true);
        root.add(progress);
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        JButton cancelButton = new JButton(L.get("core.SelfUpdater.cancel"));
        cancelButton.addActionListener(e -> {
            interrupt[0] = true;
            f.dispose();
        });
        bPanel.add(cancelButton);
        root.add(bPanel);
        f.setContentPane(root);
        f.pack();
        f.setVisible(true);
        File tmp = File.createTempFile("MangaCutter", ".jar");
        OutputStream out = new FileOutputStream(tmp);
        byte[] buffer = new byte[1024 * 256];
        int read;
        while ((read = in.read(buffer)) != -1 && !interrupt[0]) {
            out.write(buffer, 0, read);
            progress.setValue(progress.getValue() + read);
            progress.setString((progress.getValue() / 1024) + "KB / " + (progress.getMaximum() / 1024) + "KB");
        }
        if (interrupt[0]) {
            f.dispose();
            return;
        }
        in.close();
        out.close();
        File thisJar = new File(SelfUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Files.copy(Paths.get(tmp.getAbsolutePath()), new FileOutputStream(thisJar));
        new ProcessBuilder(System.getProperty("java.home") + File.separator + "bin" +
                File.separator + "java", "-jar", thisJar.getAbsolutePath()).start();
        System.exit(0);
    }
}
