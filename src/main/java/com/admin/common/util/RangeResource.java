package com.admin.common.util;

import org.springframework.core.io.Resource;

import java.io.*;
import java.net.*;

public class RangeResource implements Resource {
    private final InputStream inputStream;
    private final long contentLength;

    public RangeResource(InputStream inputStream, long contentLength) {
        this.inputStream = inputStream;
        this.contentLength = contentLength;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public File getFile() throws IOException {
        throw new UnsupportedOperationException("Not a file");
    }

    @Override
    public long contentLength() throws IOException {
        return contentLength;
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new UnsupportedOperationException("Cannot create relative resource");
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Range Resource (length: " + contentLength + ")";
    }

    @Override
    public URI getURI() throws IOException {
        throw new UnsupportedOperationException("Not supported for range resource");
    }

    @Override
    public URL getURL() throws IOException {
        throw new UnsupportedOperationException("Not supported for range resource");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RangeResource that = (RangeResource) obj;
        return contentLength == that.contentLength;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(contentLength);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}