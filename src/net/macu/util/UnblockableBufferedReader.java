package net.macu.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Reads text from a character-input stream, buffering characters so as to
 * provide for the efficient reading of characters, arrays, and lines.
 *
 * <p> The buffer size may be specified, or the default size may be used.  The
 * default is large enough for most purposes.
 *
 * <p> In general, each read request made of a Reader causes a corresponding
 * read request to be made of the underlying character or byte stream.  It is
 * therefore advisable to wrap a BufferedReader around any Reader whose read()
 * operations may be costly, such as FileReaders and InputStreamReaders.  For
 * example,
 *
 * <pre>
 * BufferedReader in
 *   = new BufferedReader(new FileReader("foo.in"));
 * </pre>
 * <p>
 * will buffer the input from the specified file.  Without buffering, each
 * invocation of read() or readLine() could cause bytes to be read from the
 * file, converted into characters, and then returned, which can be very
 * inefficient.
 *
 * <p> Programs that use DataInputStreams for textual input can be localized by
 * replacing each DataInputStream with an appropriate BufferedReader.
 *
 * @author Mark Reinhold
 * @see FileReader
 * @see InputStreamReader
 * @see java.nio.file.Files#newBufferedReader
 * @since JDK1.1
 */

public class UnblockableBufferedReader extends InputStream {

    private static final int INVALIDATED = -2;
    private static final int UNMARKED = -1;
    private static final int defaultCharBufferSize = 8192;
    private static final int defaultExpectedLineLength = 80;
    private final Object lock;
    /**
     * If the next character is a line feed, skip it
     */
    private final boolean skipLF = false;
    /**
     * The skipLF flag when the mark was set
     */
    private final boolean markedSkipLF = false;
    private InputStream in;
    private byte[] cb;
    private int nChars, nextChar;
    private int markedChar = UNMARKED;
    private int readAheadLimit = 0; /* Valid only when markedChar > 0 */

    /**
     * Creates a buffering character-input stream that uses an input buffer of
     * the specified size.
     *
     * @param in A Reader
     * @param sz Input-buffer size
     * @throws IllegalArgumentException If {@code sz <= 0}
     */
    public UnblockableBufferedReader(InputStream in, int sz) throws IOException {
        if (sz <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        this.in = in;
        cb = new byte[sz];
        nextChar = nChars = 0;
        lock = new Object();
        in.available();
    }

    /**
     * Creates a buffering character-input stream that uses a default-sized
     * input buffer.
     *
     * @param in A Reader
     */
    public UnblockableBufferedReader(InputStream in) throws IOException {
        this(in, defaultCharBufferSize);
    }

    /**
     * Fills the input buffer, taking the mark into account if it is valid.
     */
    private void fill() throws IOException {
        int dst;
        if (markedChar <= UNMARKED) {
            /* No mark */
            dst = 0;
        } else {
            /* Marked */
            int delta = nextChar - markedChar;
            if (delta >= readAheadLimit) {
                /* Gone past read-ahead limit: Invalidate mark */
                markedChar = INVALIDATED;
                readAheadLimit = 0;
                dst = 0;
            } else {
                if (readAheadLimit <= cb.length) {
                    /* Shuffle in the current buffer */
                    System.arraycopy(cb, markedChar, cb, 0, delta);
                    markedChar = 0;
                    dst = delta;
                } else {
                    /* Reallocate buffer to accommodate read-ahead limit */
                    byte[] ncb = new byte[readAheadLimit];
                    System.arraycopy(cb, markedChar, ncb, 0, delta);
                    cb = ncb;
                    markedChar = 0;
                    dst = delta;
                }
                nextChar = nChars = delta;
            }
        }

        int n;
        do {
            n = in.read(cb, dst, cb.length - dst);
        } while (n == 0);
        if (n > 0) {
            nChars = dst + n;
            nextChar = dst;
        }
    }

    public String readLine(boolean withLF) throws IOException {
        if (!withLF) {
            String s = readLine();
            if (s == null) return null;
            char[] string = s.toCharArray();
            for (int i = string.length - 1; i >= 0; i--) {
                if (string[i] != '\r' && string[i] != '\n') {
                    return s.substring(0, i + 1);
                }
            }
            return "";
        } else return readLine();
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return A String containing the contents of the line, not including
     * any line-termination characters, or null if the end of the
     * stream has been reached
     * @throws IOException If an I/O error occurs
     * @see java.io.LineNumberReader#readLine()
     */
    private String readLine() throws IOException {
        StringBuilder s = null;
        int startChar;

        synchronized (lock) {
            bufferLoop:
            for (; ; ) {

                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars) { /* EOF */
                    if (s != null && s.length() > 0)
                        return s.toString();
                    else
                        return null;
                }
                boolean eol = false;
                byte c = 0;
                int i;

                charLoop:
                for (i = nextChar; i < nChars; i++) {
                    c = cb[i];
                    if (c == '\n') {
                        eol = true;
                        i++;
                        break charLoop;
                    }
                    if (c == '\r') {
                        if (i != nChars - 1 && cb[i + 1] == '\n') {
                            i++;
                            i++;
                            eol = true;
                            break charLoop;
                        } else {
                            if (s == null) {
                                s = new StringBuilder(defaultExpectedLineLength);
                                s.append(new String(cb, nextChar, i - nextChar));
                            } else {
                                s.append(new String(cb, nextChar, i - nextChar, StandardCharsets.UTF_8));
                            }
                        }
                        continue bufferLoop;
                    }
                }

                startChar = nextChar;
                nextChar = i;

                if (eol) {
                    String str;
                    if (s == null) {
                        str = new String(cb, startChar, i - startChar, StandardCharsets.UTF_8);
                    } else {
                        s.append(new String(cb, startChar, i - startChar, StandardCharsets.UTF_8));
                        str = s.toString();
                    }
                    return str;
                }

                if (s == null)
                    s = new StringBuilder(defaultExpectedLineLength);
                s.append(new String(cb, startChar, i - startChar, StandardCharsets.UTF_8));
            }
        }
    }

    private int read1(byte[] cbuf, int off, int len) throws IOException {
        if (nextChar >= nChars) {
            fill();
        }
        if (nextChar >= nChars) return -1;
        int n = Math.min(len, nChars - nextChar);
        System.arraycopy(cb, nextChar, cbuf, off, n);
        nextChar += n;
        return n;
    }

    /**
     * Reads characters into a portion of an array.
     *
     * <p> This method implements the general contract of the corresponding
     * <code>{@link Reader#read(char[], int, int) read}</code> method of the
     * <code>{@link Reader}</code> class.  As an additional convenience, it
     * attempts to read as many characters as possible by repeatedly invoking
     * the <code>read</code> method of the underlying stream.  This iterated
     * <code>read</code> continues until one of the following conditions becomes
     * true: <ul>
     *
     * <li> The specified number of characters have been read,
     *
     * <li> The <code>read</code> method of the underlying stream returns
     * <code>-1</code>, indicating end-of-file, or
     *
     * <li> The <code>ready</code> method of the underlying stream
     * returns <code>false</code>, indicating that further input requests
     * would block.
     *
     * </ul> If the first <code>read</code> on the underlying stream returns
     * <code>-1</code> to indicate end-of-file then this method returns
     * <code>-1</code>.  Otherwise this method returns the number of characters
     * actually read.
     *
     * <p> Subclasses of this class are encouraged, but not required, to
     * attempt to read as many characters as possible in the same fashion.
     *
     * <p> Ordinarily this method takes characters from this stream's character
     * buffer, filling it from the underlying stream as necessary.  If,
     * however, the buffer is empty, the mark is not valid, and the requested
     * length is at least as large as the buffer, then this method will read
     * characters directly from the underlying stream into the given array.
     * Thus redundant <code>BufferedReader</code>s will not copy data
     * unnecessarily.
     *
     * @param cbuf Destination buffer
     * @param off  Offset at which to start storing characters
     * @param len  Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the
     * stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read(byte[] cbuf, int off, int len) throws IOException {
        synchronized (lock) {
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int n = read1(cbuf, off, len);
            if (n <= 0) return n;
            while ((n < len) && in.available() != 0) {
                int n1 = read1(cbuf, off + n, len - n);
                if (n1 <= 0) break;
                n += n1;
            }
            return n;
        }
    }


    public void close() throws IOException {
        synchronized (lock) {
            if (in == null)
                return;
            try {
                in.close();
            } finally {
                in = null;
                cb = null;
            }
        }
    }

    @Override
    public int read() throws IOException {
        byte[] arr = new byte[1];
        if (read(arr) == 1) return arr[0];
        else return -1;
    }

    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    public int available() throws IOException {
        int available = in.available();
        return available != 0 ? available + nChars - nextChar : nChars - nextChar;
    }
}
