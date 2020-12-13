package net.macu.browser.image_proxy.proxy;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class ExtendableX509KeyManager implements X509KeyManager {
    private static ExtendableX509KeyManager X509KeyManager = null;
    private final Vector<X509KeyManager> keyManagers = new Vector<>();
    private final Vector<String> domains = new Vector<>();

    private ExtendableX509KeyManager() {
    }

    public static void addDomain(String domain) throws Exception {
        ExtendableX509KeyManager km = getInstance();
        if (!km.domains.contains(domain)) {
            km.domains.add(domain);
            KeyStore store = CertificateAuthority.getRootCA().generateSubKeyStore(km.domains);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(store, null);
            km.keyManagers.clear();
            for (KeyManager keyManager : kmf.getKeyManagers()) {
                if (keyManager instanceof X509KeyManager) {
                    km.keyManagers.add((X509KeyManager) keyManager);
                }
            }
        }
    }

    public static ExtendableX509KeyManager getInstance() {
        if (X509KeyManager == null) X509KeyManager = new ExtendableX509KeyManager();
        return X509KeyManager;
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        final String[] ret = {null};
        keyManagers.forEach(manager -> {
            String alias = manager.chooseClientAlias(keyType, issuers, socket);
            if (alias != null) {
                ret[0] = alias;
            }
        });
        return ret[0];
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        final String[] ret = {null};
        keyManagers.forEach(manager -> {
            String alias = manager.chooseServerAlias(keyType, issuers, socket);
            if (alias != null) {
                ret[0] = alias;
            }
        });
        return ret[0];
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        final PrivateKey[] ret = {null};
        keyManagers.forEach(manager -> {
            PrivateKey privateKey = manager.getPrivateKey(alias);
            if (privateKey != null) {
                ret[0] = privateKey;
            }
        });
        return ret[0];
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        final X509Certificate[][] ret = {null};
        keyManagers.forEach(manager -> {
            X509Certificate[] chain = manager.getCertificateChain(alias);
            if (chain != null && chain.length > 0) {
                ret[0] = chain;
            }
        });
        return ret[0];
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        ArrayList<String> result = new ArrayList<>();
        keyManagers.forEach(manager -> {
            String[] aliases = manager.getClientAliases(keyType, issuers);
            if (aliases != null)
                result.addAll(Arrays.asList(aliases));
        });
        if (!result.isEmpty())
            return (String[]) result.toArray();
        else
            return null;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        ArrayList<String> result = new ArrayList<>();
        keyManagers.forEach(manager -> {
            String[] aliases = manager.getServerAliases(keyType, issuers);
            if (aliases != null)
                result.addAll(Arrays.asList(aliases));
        });
        if (!result.isEmpty())
            return (String[]) result.toArray();
        else
            return null;
    }
}
