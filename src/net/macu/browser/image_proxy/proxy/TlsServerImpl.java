package net.macu.browser.image_proxy.proxy;

import org.bouncycastle.tls.*;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.tls.crypto.TlsCryptoParameters;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCertificate;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCrypto;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsECDSASigner;
import org.bouncycastle.tls.crypto.impl.jcajce.JceDefaultTlsCredentialedDecryptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

public class TlsServerImpl extends DefaultTlsServer {
    CertificateAuthority genCa;
    Object _lock = new Object();


    public TlsServerImpl(String targetHost) {
        super(CertificateAuthority.getCrypto());
        try {
            System.out.println(Thread.currentThread().getName() + " started sub cert creation at time: " + Calendar.getInstance().getTime().toString());
            genCa = CertificateAuthority.getRootCA().issueSubCertificate(Arrays.asList(targetHost));
            System.out.println(Thread.currentThread().getName() + " ended sub cert creation at time: " + Calendar.getInstance().getTime().toString());
//            PrintWriter fos = new PrintWriter(new FileOutputStream("sub.crt"));
//            fos.print(genCa.getCertificateChainBase64Encoded());
//            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public TlsCredentials getCredentials() throws IOException {
        return this.getECDSASignerCredentials();
    }

    @Override
    protected TlsCredentialedSigner getECDSASignerCredentials() throws IOException {
        TlsCertificate[] chain = new TlsCertificate[genCa.getCertificateChain().length];

        for (int i = 0; i < chain.length; i++) {
            chain[i] = new JcaTlsCertificate((JcaTlsCrypto) getCrypto(), genCa.getCertificateChain()[i]);
        }
        Certificate cert = new Certificate(chain);
        return new DefaultTlsCredentialedSigner(new TlsCryptoParameters(context),
                new JcaTlsECDSASigner((JcaTlsCrypto) CertificateAuthority.getCrypto(), genCa.getPrivate()),
                cert, new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa));
//        return new JcaDefaultTlsCredentialedSigner(new TlsCryptoParameters(context), (JcaTlsCrypto) CertificateAuthority.getCrypto(),genCa.getPrivate(), cert, new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa));
//        return new JcaDefaultTlsCredentialedSigner((JcaTlsCrypto) getCrypto(), cert, genCa.getPrivate());
    }

    @Override
    protected TlsCredentialedDecryptor getRSAEncryptionCredentials() throws IOException {

        TlsCertificate[] chain = new TlsCertificate[genCa.getCertificateChain().length];

        for (int i = 0; i < chain.length; i++) {
            chain[i] = new JcaTlsCertificate((JcaTlsCrypto) getCrypto(), genCa.getCertificateChain()[i]);
        }
        Certificate cert = new Certificate(chain);
        return new JceDefaultTlsCredentialedDecryptor((JcaTlsCrypto) getCrypto(), cert, genCa.getPrivate());
    }

    @Override
    protected int[] getSupportedCipherSuites() {
        return new int[]{
                /*CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256*/
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
        };
    }

    public void addHost(String name) {

    }
}
