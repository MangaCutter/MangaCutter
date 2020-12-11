package net.macu.browser.image_proxy.proxy;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class CertKeyPair {
    private final X509Certificate certificate;
    private final KeyPair kp;

    public CertKeyPair(X509Certificate certificate, KeyPair kp) {

        this.certificate = certificate;
        this.kp = kp;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public KeyPair getKeyPair() {
        return kp;
    }
}
