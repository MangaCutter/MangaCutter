package net.macu.browser.image_proxy.proxy;

import org.bouncycastle.asn1.ASN1Encodable;
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
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.bc.BcDefaultDigestProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.*;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilderProvider;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.Streams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;

public class CertUtils {
    //    private static CertKeyPair rootCKP;
    private static final char[] MAIN_PASSWORD = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String BC_PROVIDER = "BC";

    public static KeyStore generate(String domain, CertKeyPair ckp) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", BC_PROVIDER);
        keyPairGenerator.initialize(1024);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DATE, 3);
        Date endDate = calendar.getTime();
        BigInteger issuedCertSerialNum = new BigInteger(Long.toString(new SecureRandom().nextLong()));
        KeyPair issuedCertKeyPair = keyPairGenerator.generateKeyPair();
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(new X500Name("CN=" + domain), issuedCertKeyPair.getPublic());
        JcaContentSignerBuilder csrBuilder = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC_PROVIDER);
        ContentSigner csrContentSigner = csrBuilder.build(ckp.getKeyPair().getPrivate());
        PKCS10CertificationRequest csr = p10Builder.build(csrContentSigner);
        X509v3CertificateBuilder issuedCertBuilder = new X509v3CertificateBuilder(new X500Name(ckp.getCertificate().getSubjectDN().getName()), issuedCertSerialNum, startDate, endDate, csr.getSubject(), csr.getSubjectPublicKeyInfo());
        JcaX509ExtensionUtils issuedCertExtUtils = new JcaX509ExtensionUtils();
        issuedCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        issuedCertBuilder.addExtension(Extension.authorityKeyIdentifier, false, issuedCertExtUtils.createAuthorityKeyIdentifier(ckp.getCertificate()));
        issuedCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, issuedCertExtUtils.createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()));
        issuedCertBuilder.addExtension(Extension.keyUsage, false, new KeyUsage(KeyUsage.keyEncipherment));
        issuedCertBuilder.addExtension(Extension.subjectAlternativeName, false, new DERSequence(new ASN1Encodable[]{new GeneralName(GeneralName.dNSName, domain)}));
        X509CertificateHolder issuedCertHolder = issuedCertBuilder.build(csrContentSigner);
        X509Certificate issuedCert = new JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(issuedCertHolder);
        issuedCert.verify(ckp.getCertificate().getPublicKey(), BC_PROVIDER);
        KeyStore sslKeyStore = KeyStore.getInstance("PKCS12", BC_PROVIDER);
        sslKeyStore.load(null, null);
        sslKeyStore.setKeyEntry("alias", issuedCertKeyPair.getPrivate(), null, new java.security.cert.Certificate[]{issuedCert, ckp.getCertificate()});
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        sslKeyStore.store(baos, MAIN_PASSWORD);
//        KeyStore resultKS = KeyStore.getInstance(KeyStore.getDefaultType());
//        resultKS.load(new ByteArrayInputStream(baos.toByteArray()), MAIN_PASSWORD);
//        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        kmf.init(sslKeyStore, null);
        return sslKeyStore;
    }

    public static CertKeyPair readPKCS12File(InputStream pfxIn) throws Exception {
        PKCS12PfxPdu pfx = new PKCS12PfxPdu(Streams.readAll(pfxIn));

        if (!pfx.isMacValid(new BcPKCS12MacCalculatorBuilderProvider(BcDefaultDigestProvider.INSTANCE), MAIN_PASSWORD)) {
            throw new CertException("PKCS#12 MAC test failed!");
        }

        ContentInfo[] infos = pfx.getContentInfos();

        InputDecryptorProvider inputDecryptorProvider = new JcePKCSPBEInputDecryptorProviderBuilder()
                .setProvider(BC_PROVIDER).build(MAIN_PASSWORD);
        JcaX509CertificateConverter jcaConverter = new JcaX509CertificateConverter().setProvider(BC_PROVIDER);
        X509Certificate certificate = null;
        KeyPair kp = null;
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
                PrivateKey privKey = keyFact.generatePrivate(new PKCS8EncodedKeySpec(info.getEncoded()));
                kp = new KeyPair(certificate.getPublicKey(), privKey);
                /*org.bouncycastle.asn1.pkcs.Attribute[] attributes = bags[0].getAttributes();
                for (int a = 0; a != attributes.length; a++) {
                    Attribute attr = attributes[a];

                    if (attr.getAttrType().equals(PKCS12SafeBag.friendlyNameAttribute)) {
                        privKeyMap.put(((DERBMPString) attr.getAttributeValues()[0]).getString(), privKey);
                    } else if (attr.getAttrType().equals(PKCS12SafeBag.localKeyIdAttribute)) {
                        privKeyIds.put(privKey, attr.getAttributeValues()[0]);
                    }
                }*/
            }
        }
        /*for (Iterator it = privKeyMap.keySet().iterator(); it.hasNext(); ) {
            String alias = (String) it.next();

            System.out.println("Key Entry: " + alias + ", Subject: " + (((X509Certificate) certKeyIds.get(privKeyIds.get(privKeyMap.get(alias)))).getSubjectDN()));
        }

        for (Iterator it = certMap.keySet().iterator(); it.hasNext(); ) {
            String alias = (String) it.next();

            System.out.println("Certificate Entry: " + alias + ", Subject: " + (((X509Certificate) certMap.get(alias)).getSubjectDN()));
        }
        System.out.println();*/
        return new CertKeyPair(certificate, kp);
    }

    public static String certToBase64String(Certificate[] chain) throws CertificateEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Certificate certificate : chain) {
            sb.append("-----BEGIN CERTIFICATE-----");
            sb.append(Base64.toBase64String(certificate.getEncoded()));
            sb.append("-----END CERTIFICATE-----\n");
        }
        return sb.toString();
    }

    public static String exportKeyPairToKeystoreFileBase64Encoded(KeyPair keyPair, Certificate certificate, String alias, String storeType, String storePass) throws Exception {
        KeyStore sslKeyStore = KeyStore.getInstance(storeType, BC_PROVIDER);
        sslKeyStore.load(null, null);
        sslKeyStore.setKeyEntry(alias, keyPair.getPrivate(), null, new Certificate[]{certificate});
        ByteArrayOutputStream keyStoreOs = new ByteArrayOutputStream();
        sslKeyStore.store(keyStoreOs, storePass.toCharArray());
        return Base64.toBase64String(keyStoreOs.toByteArray());
    }
}
