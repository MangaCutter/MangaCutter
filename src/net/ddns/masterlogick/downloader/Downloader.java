package net.ddns.masterlogick.downloader;

import java.awt.image.BufferedImage;
import java.util.List;

public interface Downloader {
    BufferedImage[] downloadFragments(List<String> paths);

    void cancel();
}
