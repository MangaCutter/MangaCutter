package net.macu.downloader;

import net.macu.UI.ViewManager;
import org.apache.http.client.methods.HttpUriRequest;

import java.awt.image.BufferedImage;
import java.util.List;

public interface Downloader {

    BufferedImage[] downloadFragments(List<HttpUriRequest> requests, ViewManager viewManager);

    void cancel();
}
