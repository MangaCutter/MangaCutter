package net.macu.browser.proxy.cert;

import net.macu.UI.ViewManager;
import net.macu.settings.L;
import net.macu.settings.Parameter;
import net.macu.settings.Parameters;
import net.macu.settings.Parametrized;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.digest.SHA1;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.bc.BcDefaultDigestProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.*;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilderProvider;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.tls.crypto.TlsCrypto;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCrypto;
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCryptoProvider;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class CertificateAuthority implements Parametrized {
    private static final Parameter ROOT_CA = new Parameter(Parameter.Type.STRING_TYPE, "browser.proxy.cert.CertificateAuthority.root_ca");
    private static CertificateAuthority rootCA = null;
    private static final char[] MAIN_PASSWORD = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String BC_PROVIDER = "BC";
    private static final String KET_STORE_TYPE = "PKCS12";
    private static final JcaTlsCrypto crypto = new JcaTlsCryptoProvider().create(new InsecureSecureRandom());

    private final X509Certificate[] certificateChain;
    private final PrivateKey privateKey;

    public CertificateAuthority(X509Certificate[] certificateChain, PrivateKey privateKey) {
        this.certificateChain = certificateChain;
        this.privateKey = privateKey;
    }

    public static CertificateAuthority readPKCS12File(byte[] PKCS12Bytes) throws Exception {
        PKCS12PfxPdu pfx = new PKCS12PfxPdu(PKCS12Bytes);
        if (!pfx.isMacValid(new BcPKCS12MacCalculatorBuilderProvider(BcDefaultDigestProvider.INSTANCE), MAIN_PASSWORD)) {
            throw new CertException("PKCS#12 MAC test failed!");
        }
        ContentInfo[] infos = pfx.getContentInfos();
        InputDecryptorProvider inputDecryptorProvider = new JcePKCSPBEInputDecryptorProviderBuilder().setProvider(BC_PROVIDER).build(MAIN_PASSWORD);
        JcaX509CertificateConverter jcaConverter = new JcaX509CertificateConverter().setProvider(BC_PROVIDER);
        X509Certificate certificate = null;
        PrivateKey privateKey = null;
        for (int i = 0; i != infos.length; i++) {
            if (infos[i].getContentType().equals(PKCSObjectIdentifiers.encryptedData)) {
                PKCS12SafeBagFactory dataFact = new PKCS12SafeBagFactory(infos[i], inputDecryptorProvider);
                PKCS12SafeBag[] bags = dataFact.getSafeBags();
                for (int b = 0; b != bags.length; b++) {
                    PKCS12SafeBag bag = bags[b];
                    X509CertificateHolder certHldr = (X509CertificateHolder) bag.getBagValue();
                    X509Certificate cert = jcaConverter.getCertificate(certHldr);
                    org.bouncycastle.asn1.pkcs.Attribute[] attributes = bag.getAttributes();
                    for (int a = 0; a != attributes.length; a++) {
                        org.bouncycastle.asn1.pkcs.Attribute attr = attributes[a];
                        if (attr.getAttrType().equals(PKCS12SafeBag.localKeyIdAttribute)) {
                            certificate = cert;
                        }
                    }
                }
            } else {
                PKCS12SafeBagFactory dataFact = new PKCS12SafeBagFactory(infos[i]);
                PKCS12SafeBag[] bags = dataFact.getSafeBags();
                PKCS8EncryptedPrivateKeyInfo encInfo = (PKCS8EncryptedPrivateKeyInfo) bags[0].getBagValue();
                PrivateKeyInfo info = encInfo.decryptPrivateKeyInfo(inputDecryptorProvider);
                KeyFactory keyFact = KeyFactory.getInstance(info.getPrivateKeyAlgorithm().getAlgorithm().getId(), BC_PROVIDER);
                privateKey = keyFact.generatePrivate(new PKCS8EncodedKeySpec(info.getEncoded()));
            }
        }
        return new CertificateAuthority(new X509Certificate[]{certificate}, privateKey);
    }

    public static void loadRootCA() {
        if (ROOT_CA.getString() == null || ROOT_CA.getString().isEmpty()) {
            ViewManager.showMessageDialog(L.get("browser.proxy.cert.CertificateAuthority.openGenerateCertFrame.saved_certificate_is_empty"), null);
            return;
        }
        try {
            rootCA = readPKCS12File(Base64.decode(ROOT_CA.getString()));
        } catch (NullPointerException e) {
            ViewManager.showMessageDialog(L.get("browser.proxy.cert.CertificateAuthority.openGenerateCertFrame.saved_certificate_is_empty"), null);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showMessageDialog(L.get("browser.proxy.cert.CertificateAuthority.openGenerateCertFrame.certificate_loading_failed", e.toString()), null);
        }
    }

    public static CertificateAuthority getRootCA() {
        return rootCA;
    }

    public synchronized static void openGenerateCertFrame() {
        String path = ViewManager.requestChooseSingleFile("crt", null);
        if (path != null) {
            try {
                CertificateAuthority newRoot = generateNewRootCA();
                FileWriter out = new FileWriter(path);
                out.append(newRoot.getCertificateChainBase64Encoded());
                out.flush();
                ROOT_CA.setValue(newRoot.getKeyPairKeystoreFileBase64Encoded("alias"));
                rootCA = newRoot;
                ViewManager.showMessageDialog(L.get("browser.proxy.cert.CertificateAuthority.openGenerateCertFrame.certificate_generated", newRoot.getSHA256Fingerprint()), null);
            } catch (Exception e) {
                ViewManager.showMessageDialog(L.get("browser.proxy.cert.CertificateAuthority.openGenerateCertFrame.certificate_generation_failed", e.toString()), null);
                e.printStackTrace();
            }
        }
    }

    public static void openExportCertificateFrame() {
        String path = ViewManager.requestChooseSingleFile("crt", null);
        if (path != null) {
            if (rootCA != null) {
                try {
                    FileWriter out = new FileWriter(path);
                    out.append(rootCA.getCertificateChainBase64Encoded());
                    out.flush();
                } catch (IOException | CertificateEncodingException e) {
                    e.printStackTrace();
                    ViewManager.showMessageDialog(L.get("browser.proxy.cert.CertificateAuthority.openExportCertificateFrame.certificate_export_failed"), null);
                }
            } else {
                ViewManager.showMessageDialog(L.get("browser.proxy.cert.CertificateAuthority.openExportCertificateFrame.certificate_does_not_exist"), null);
            }
        }
    }

    public static TlsCrypto getCrypto() {
        return crypto;
    }

    public static CertificateAuthority generateNewRootCA() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", BC_PROVIDER);
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"), crypto.getSecureRandom());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 1000);
        Date endDate = calendar.getTime();
        KeyPair rootKeyPair = keyPairGenerator.generateKeyPair();
        BigInteger rootSerialNum = new BigInteger(Long.toString(crypto.getSecureRandom().nextLong()));
        X500Name rootCertIssuer = new X500Name("C=AQ,ST=South-Pole,O=MangaCutter Team,CN=MangaCutter EC Root CA");
        ContentSigner rootCertContentSigner = new JcaContentSignerBuilder("SHA256withECDSA").setProvider(BC_PROVIDER).build(rootKeyPair.getPrivate());
        X509v3CertificateBuilder rootCertBuilder = new JcaX509v3CertificateBuilder(rootCertIssuer, rootSerialNum, startDate, endDate, rootCertIssuer, rootKeyPair.getPublic());
        JcaX509ExtensionUtils rootCertExtUtils = new JcaX509ExtensionUtils();
        rootCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        rootCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, rootCertExtUtils.createSubjectKeyIdentifier(rootKeyPair.getPublic()));
        rootCertBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign));
        X509CertificateHolder rootCertHolder = rootCertBuilder.build(rootCertContentSigner);
        X509Certificate rootCert = new JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(rootCertHolder);
        return new CertificateAuthority(new X509Certificate[]{rootCert}, rootKeyPair.getPrivate());
    }

    public X509Certificate[] getCertificateChain() {
        return certificateChain;
    }

    public PrivateKey getPrivate() {
        return privateKey;
    }

    public static Parameters getParameters() {
        return new Parameters("browser.proxy.cert.CertificateAuthority", ROOT_CA);
    }

    public KeyStore issueSubKeyStore(List<String> domains) throws Exception {
        CertificateAuthority issued = issueSubCertificate(domains);
        KeyStore sslKeyStore = KeyStore.getInstance(KET_STORE_TYPE, BC_PROVIDER);
        sslKeyStore.load(null, null);
        sslKeyStore.setKeyEntry("server-cert", issued.getPrivate(), null, issued.getCertificateChain());
        return sslKeyStore;
    }

    public CertificateAuthority issueSubCertificate(List<String> domains) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", BC_PROVIDER);
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"), crypto.getSecureRandom());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DATE, 3);
        Date endDate = calendar.getTime();
        BigInteger issuedCertSerialNum = new BigInteger(Long.toString(crypto.getSecureRandom().nextLong()));
        KeyPair issuedCertKeyPair = keyPairGenerator.generateKeyPair();
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(new X500Name("CN=" + domains.get(0)), issuedCertKeyPair.getPublic());
        JcaContentSignerBuilder csrBuilder = new JcaContentSignerBuilder("SHA256withECDSA").setProvider(BC_PROVIDER);
        ContentSigner csrContentSigner = csrBuilder.build(privateKey);
        PKCS10CertificationRequest csr = p10Builder.build(csrContentSigner);
        X509v3CertificateBuilder issuedCertBuilder = new X509v3CertificateBuilder(new X500Name(certificateChain[0].getSubjectDN().getName()), issuedCertSerialNum, startDate, endDate, csr.getSubject(), csr.getSubjectPublicKeyInfo());
        JcaX509ExtensionUtils issuedCertExtUtils = new JcaX509ExtensionUtils();
        issuedCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        issuedCertBuilder.addExtension(Extension.authorityKeyIdentifier, false, issuedCertExtUtils.createAuthorityKeyIdentifier(certificateChain[0]));
        issuedCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, issuedCertExtUtils.createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()));
        issuedCertBuilder.addExtension(Extension.keyUsage, false, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyAgreement));
        GeneralName[] names = new GeneralName[domains.size()];
        for (int i = 0; i < domains.size(); i++) {
            names[i] = new GeneralName(GeneralName.dNSName, domains.get(i));
        }
        issuedCertBuilder.addExtension(Extension.subjectAlternativeName, false, new DERSequence(names));
        X509CertificateHolder issuedCertHolder = issuedCertBuilder.build(csrContentSigner);
        X509Certificate issuedCert = new JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(issuedCertHolder);
        issuedCert.verify(certificateChain[0].getPublicKey(), BC_PROVIDER);
        ArrayList<X509Certificate> issuedCertificateChain = new ArrayList<>();
        issuedCertificateChain.add(issuedCert);
        Collections.addAll(issuedCertificateChain, certificateChain);
        return new CertificateAuthority(issuedCertificateChain.toArray(new X509Certificate[0]), issuedCertKeyPair.getPrivate());
    }

    public String getCertificateChainBase64Encoded() throws CertificateEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Certificate certificate : certificateChain) {
            sb.append("-----BEGIN CERTIFICATE-----\n");
            sb.append(Base64.toBase64String(certificate.getEncoded()));
            sb.append("\n");
            sb.append("-----END CERTIFICATE-----\n");
        }
        return sb.toString();
    }

    public String getKeyPairKeystoreFileBase64Encoded(String alias) throws Exception {
        KeyStore sslKeyStore = KeyStore.getInstance(KET_STORE_TYPE, BC_PROVIDER);
        sslKeyStore.load(null, null);
        sslKeyStore.setKeyEntry(alias, privateKey, null, certificateChain);
        ByteArrayOutputStream keyStoreOs = new ByteArrayOutputStream();
        sslKeyStore.store(keyStoreOs, MAIN_PASSWORD);
        return Base64.toBase64String(keyStoreOs.toByteArray());
    }

    public String getSHA256Fingerprint() {
        byte[] cert;
        try {
            cert = certificateChain[0].getEncoded();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
            return "INVALID_CERTIFICATE_DO_NOT_ADD_IT_TO_TRUSTED_ROOT_CA_CONTACT_WITH_DEVELOPERS";
        }
        StringBuilder sig = new StringBuilder();
        for (byte b : new SHA1.Digest().digest(cert)) {
            sig.append(String.format("%02x", Byte.toUnsignedInt(b)).toUpperCase()).append(":");
        }
        sig.deleteCharAt(sig.length() - 1);
        return sig.toString();
    }
}
