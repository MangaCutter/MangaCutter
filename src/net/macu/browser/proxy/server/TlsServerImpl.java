package net.macu.browser.proxy.server;

import net.macu.browser.proxy.cert.CertificateAuthority;
import org.bouncycastle.tls.*;
import org.bouncycastle.tls.crypto.TlsCertificate;
import org.bouncycastle.tls.crypto.TlsCryptoParameters;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCertificate;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCrypto;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsECDSASigner;
import org.bouncycastle.tls.crypto.impl.jcajce.JceDefaultTlsCredentialedDecryptor;

import java.io.IOException;
import java.util.Arrays;

public class TlsServerImpl extends DefaultTlsServer {
    CertificateAuthority genCa;


    public TlsServerImpl(String targetHost) {
        super(CertificateAuthority.getCrypto());
        try {
            genCa = CertificateAuthority.getRootCA().issueSubCertificate(Arrays.asList(targetHost));
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
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
        };
    }
}
