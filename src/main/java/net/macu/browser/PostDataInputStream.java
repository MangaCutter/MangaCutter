package net.macu.browser;

import org.cef.network.CefPostDataElement;

import java.io.InputStream;

public class PostDataInputStream extends InputStream {
    final int bytesCount;
    final CefPostDataElement postEntity;
    byte[] single = new byte[1];
    int pos = 0;

    public PostDataInputStream(CefPostDataElement postEntity) {
        this.postEntity = postEntity;
        bytesCount = postEntity.getBytesCount();
    }

    @Override
    public int read() {
        if (pos > bytesCount) return -1;
        while (postEntity.getBytes(1, single) == 0) {
        }
        pos++;
        return Byte.toUnsignedInt(single[0]);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (pos > bytesCount) return -1;
        if (b.length == 0) return 0;
        byte[] tmp = new byte[len];
        int oldLen = len;
        while (len > 0 || pos > bytesCount) {
            int read = postEntity.getBytes(len, tmp);
            if (read == 0 && len != oldLen) return oldLen - len;
            len -= read;
            pos += read;
            System.arraycopy(tmp, 0, b, off, read);
            off += read;
        }
        return oldLen - len;
    }
}
