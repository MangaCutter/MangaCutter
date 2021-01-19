package net.macu.core;

import net.macu.UI.IconManager;
import net.macu.settings.L;
import org.apache.http.HttpEntity;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;

public class SelfUpdater {
    public static void selfUpdate(String url) throws URISyntaxException, IOException {
        File thisJar = new File(SelfUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        HttpEntity entity = IOManager.sendRawRequest(url);
        InputStream in = entity.getContent();
        JFrame f = new JFrame(L.get("core.SelfUpdater.frame_title"));
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setIconImage(IconManager.getBrandIcon());
        f.setResizable(false);
        f.setLocationRelativeTo(null);
        JProgressBar progress = new JProgressBar(0, (int) entity.getContentLength());
        f.setContentPane(progress);
        f.pack();
        f.setVisible(true);
        OutputStream out = new FileOutputStream(thisJar);
        byte[] buffer = new byte[1024 * 256];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            progress.setValue(progress.getValue() + read);
        }
        in.close();
        out.close();
        new ProcessBuilder(System.getProperty("java.home") + File.separator + "bin" +
                File.separator + "java", "-jar", thisJar.getAbsolutePath()).start();
        System.exit(0);
    }
}
