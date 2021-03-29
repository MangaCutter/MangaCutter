package net.macu.service;

import net.macu.UI.ViewManager;
import net.macu.settings.L;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LocalFiles implements Service {
    private boolean cancel = false;

    private static boolean isValidURI(String uri) {
        try {
            URI.create(uri);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public BufferedImage[] parsePage(String uri, ViewManager viewManager) {
        viewManager.startProgress(1, L.get("service.LocalFiles.parsePage.progress"));
        List<File> files = Arrays.asList(new File(URI.create(uri)).listFiles());
        files.sort(Comparator.comparing(File::getName));
        return files.stream().map(file -> {
            try {
                return ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList()).toArray(new BufferedImage[0]);
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
