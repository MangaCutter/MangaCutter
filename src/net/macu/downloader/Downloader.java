package net.macu.downloader;

import net.macu.UI.ViewManager;

import java.awt.image.BufferedImage;
import java.util.List;

public interface Downloader {

    BufferedImage[] downloadFragments(List<String> paths, ViewManager viewManager);

    void cancel();
}
