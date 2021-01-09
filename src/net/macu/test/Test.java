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
        /*ArrayList<String> map = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            map.add(String.format("http://example.com/ttt/%04d.png", i));
        }
        new Request(map, new CapturedImageProcessor());*/
//        IOManager.initClient();
//        System.out.println(CertificateAuthority.readPKCS12File(Streams.readAll(new FileInputStream("/home/user/ca/ca.p12"))).getCertificateChain()[0].toString());
        /*CertificateAuthority root = CertificateAuthority.generateNewRootCA();
        PrintWriter fos = new PrintWriter(new FileOutputStream("root.crt"));
        fos.print(root.getCertificateChainBase64Encoded());
        fos.close();
        fos = new PrintWriter(new FileOutputStream("root.p12.enc"));
        fos.print(root.getKeyPairKeystoreFileBase64Encoded("alias"));
        fos.close();*/
    }

}
