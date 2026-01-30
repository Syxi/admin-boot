package com.admin.common.util;

import java.io.IOException;
import java.io.InputStream;

public class SkipInputStream extends InputStream {
    private final InputStream inputStream;
    private long position = 0;
    private final long skipBytes;

    public SkipInputStream(InputStream inputStream, long skipBytes) throws IOException {
        this.inputStream = inputStream;
        this.skipBytes = skipBytes;
        this.position = inputStream.skip(skipBytes);
    }

    @Override
    public int read() throws IOException {
        if (position >= skipBytes) {
            int b = inputStream.read();
            if (b != -1) {
                position++;
            }
            return b;
        } else {
            int b = inputStream.read();
            position++;
            return b;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (position >= skipBytes) {
            int bytesRead = inputStream.read(b, off, len);
            if (bytesRead > 0) {
                position += bytesRead;
            }
            return bytesRead;
        } else {
            int skipped = (int) Math.min(len, skipBytes - position);
            int bytesRead = inputStream.read(b, off, len - skipped);
            if (bytesRead > 0) {
                position += bytesRead;
            }
            return bytesRead;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = inputStream.skip(n);
        position += skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}