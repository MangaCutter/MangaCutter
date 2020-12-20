package net.macu.test;

import net.macu.browser.image_proxy.proxy.CertificateAuthority;
import net.macu.browser.plugin.BrowserPlugin;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class Test {

    public static void main(String[] args) throws Exception {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Info");
        Security.addProvider(new BouncyCastleProvider());
        CertificateAuthority.loadRootCA();
        BrowserPlugin bp = BrowserPlugin.getPlugin();
        bp.start();
//        IOManager.initClient();
//        System.out.println(CertificateAuthority.readPKCS12File(Streams.readAll(new FileInputStream("/home/user/ca/ca.p12"))).getCertificateChain()[0].toString());
       /* CertificateAuthority root = CertificateAuthority.generateNewRootCA();
        PrintWriter fos = new PrintWriter(new FileOutputStream("root.crt"));
        fos.print(root.getCertificateChainBase64Encoded());
        fos.close();
        fos = new PrintWriter(new FileOutputStream("root.p12.enc"));
        fos.print(root.getKeyPairKeystoreFileBase64Encoded("alias"));
        fos.close();*/
    }

}
