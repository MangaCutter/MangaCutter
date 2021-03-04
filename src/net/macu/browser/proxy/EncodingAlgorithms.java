package net.macu.browser.proxy;

import net.macu.util.RawDataReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.ProtocolException;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class EncodingAlgorithms {
    public static byte[] decompressDeflate(RawDataReader in) throws IOException, DataFormatException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Inflater inflater = new Inflater(true);
        byte[] inBuffer = new byte[1024 * 32];
        byte[] outBuffer = new byte[1024 * 32];
        int inLen, outLen;
        while ((inLen = in.read(inBuffer)) != 0) {
            inflater.setInput(inBuffer, 0, inLen);
            while ((outLen = inflater.inflate(outBuffer)) != 0) out.write(outBuffer, 0, outLen);
            if (inflater.finished()) break;
        }
        in.returnBack(inBuffer, inLen - inflater.getRemaining(), inflater.getRemaining());
        return out.toByteArray();
    }

    /**
     * Realization of gzip 4.3 decompression algorithm declared in RFC 1952
     */
    public static byte[] decompressGZIP(RawDataReader in) throws IOException, DataFormatException {
        byte[] sig = new byte[3];
        in.read(sig);
        if (sig[0] != 0x1f && sig[1] != 0x8b && sig[2] != 0x08)
            throw new ProtocolException("GZIP header field mismatch");
        int FLG = in.read();
        boolean FTEXT = (FLG & (1 << 0)) != 0;
        boolean FHCRC = (FLG & (1 << 1)) != 0;
        boolean FEXTRA = (FLG & (1 << 2)) != 0;
        boolean FNAME = (FLG & (1 << 3)) != 0;
        boolean FCOMMENT = (FLG & (1 << 4)) != 0;
        if ((FLG & 0b11100000) != 0)
            throw new ProtocolException("FLG reserved bits are non-zero:" + Integer.toBinaryString(FLG));
        long MTIME = readInt32(in);
        int XFL = in.read();
        int OS = in.read();
        byte[] extraFiled;
        if (FEXTRA) {
            extraFiled = new byte[readInt16(in)];
            in.read(extraFiled);
        }
        String name = "";
        if (FNAME) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int t;
            while ((t = in.read()) != 0) buffer.write(t);
            name = buffer.toString("ISO-8859-1");
        }
        String comment = "";
        if (FCOMMENT) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int t;
            while ((t = in.read()) != 0) buffer.write(t);
            comment = buffer.toString("ISO-8859-1");
        }
        int CRC16 = 0;
        if (FHCRC) {
            CRC16 = readInt16(in);
        }
        byte[] data = decompressDeflate(in);
        long CRC32 = readInt32(in);
        long ISIZE = readInt32(in);
        CRC32 crc = new CRC32();
        crc.update(data);
        if (crc.getValue() != CRC32)
            throw new StreamCorruptedException("CRC32 of data mismatch: in entry " + Long.toHexString(CRC32) + ", in decoded data " + Long.toHexString(crc.getValue()));
        if (data.length != ISIZE)
            throw new StreamCorruptedException("Length of data mismatch: in entry: " + ISIZE + ", in decoded data " + data.length);
        return data;
    }

    public static byte[] reassembleChunked(RawDataReader in) throws IOException {
        String buffer;
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        while ((buffer = in.readLine(false)) != null && !buffer.isEmpty()) {
            int size = Integer.parseInt(buffer, 16);
            if (size == 0) {
                in.readLine(true);
                break;
            }
            byte[] chunk = new byte[size];
            for (int filled = 0; filled < size; ) {
                filled += in.read(chunk, filled, size - filled);
            }
            body.write(chunk);
            in.readLine(true);
        }
        return body.toByteArray();
    }

    private static int readInt16(RawDataReader in) throws IOException {
        return in.read() | (in.read() << 8);
    }

    private static long readInt32(RawDataReader in) throws IOException {
        return (((long) in.read()) | ((long) in.read() << 8) | ((long) in.read() << 16) | ((long) in.read() << 24));
    }
}
