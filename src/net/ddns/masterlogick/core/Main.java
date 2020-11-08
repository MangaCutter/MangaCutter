package net.ddns.masterlogick.core;

import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.service.IOManager;

public class Main {

    public static void main(String[] args) {
        IOManager.initClient();
        ViewManager.createView();
    }
}
