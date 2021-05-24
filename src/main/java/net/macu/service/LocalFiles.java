package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.settings.L;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LocalFiles implements Service {
    private boolean cancel = false;

    private static boolean isValidURI(String uri) {
        try {
            URI u = URI.create(uri);
            return u.getScheme().equals("file");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public BufferedImage[] parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.LocalFiles.parsePage.progress"));
        File selected = new File(URI.create(uri));
        List<File> files;
        if (selected.isDirectory())
            files = Arrays.asList(selected.listFiles());
        else files = new ArrayList<>(Arrays.asList(selected));
        files.sort(Comparator.comparing(File::getName));
        return files.stream().map(file -> {
            try {
                return ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).toArray(BufferedImage[]::new);
    }

    public boolean accept(String uri) {
        return isValidURI(uri);
    }

    public String getInfo() {
        return "Local filesystem";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
