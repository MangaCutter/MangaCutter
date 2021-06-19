package net.macu.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ExtendedByteArrayOutputStream extends OutputStream {
    private final byte[] single = new byte[1];
    private final ArrayList<byte[]> fulled = new ArrayList<>();
    private ByteArrayOutputStream current = new ByteArrayOutputStream();
    private long size = 0;

    @Override
    public void write(int b) throws IOException {
        single[0] = (byte) b;
        write(single, 0, 1);
        size++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null) throw new NullPointerException();
        if (off < 0 || len < 0 || off > b.length || off + len > b.length) throw new IndexOutOfBoundsException();
        int toWrite = Math.min(Integer.MAX_VALUE - current.size(), len);
        current.write(b, off, toWrite);
        size += toWrite;
        if (current.size() == Integer.MAX_VALUE) dropCurrentToFulled();
        if (len - toWrite > 0) {
            write(b, off + toWrite, len - toWrite);
        }
    }

    public byte[][] toByteArrays() {
        if (current.size() > 0) dropCurrentToFulled();
        byte[][] result = new byte[fulled.size()][];
        for (int i = 0; i < result.length; i++) {
            result[i] = fulled.get(i);
        }
        return result;
    }

    public long getSize() {
        return size;
    }

    private void dropCurrentToFulled() {
        fulled.add(current.toByteArray());
        current = new ByteArrayOutputStream();
    }

    public void writeTo(OutputStream dst) throws IOException {
        for (byte[] array : fulled) {
            dst.write(array);
        }
        dst.write(current.toByteArray());
    }
}
