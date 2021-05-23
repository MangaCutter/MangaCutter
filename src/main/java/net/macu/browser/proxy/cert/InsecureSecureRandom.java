package net.macu.browser.proxy.cert;

import java.security.SecureRandom;
import java.util.Random;

public class InsecureSecureRandom extends SecureRandom {
    private Random r = new Random();

    @Override
    public byte[] generateSeed(int numBytes) {
        byte[] arr = new byte[numBytes];
        r.nextBytes(arr);
        return arr;
    }

    @Override
    public String getAlgorithm() {
        return "insecure";
    }

    @Override
    public void nextBytes(byte[] bytes) {
        r.nextBytes(bytes);
    }

    @Override
    public void setSeed(long seed) {
        if (r == null) r = new Random();
        r.setSeed(seed);
    }

    @Override
    public synchronized void setSeed(byte[] seed) {
        setSeed(seed[0]);
    }
}
