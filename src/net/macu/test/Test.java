package net.macu.test;

import net.macu.UI.IconManager;
import net.macu.UI.ViewManager;
import net.macu.browser.plugin.BrowserPlugin;
import net.macu.browser.proxy.cert.CertificateAuthority;
import net.macu.settings.L;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class Test {

    public static void main(String[] args) throws Exception {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Info");
        ViewManager.setLookAndFeel();
        L.loadLanguageData();
        Security.addProvider(new BouncyCastleProvider());
        IconManager.loadIcons();
        CertificateAuthority.loadRootCA();
        BrowserPlugin bp = BrowserPlugin.getPlugin();
        bp.start();
    }

}
