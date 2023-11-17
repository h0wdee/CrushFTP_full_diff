/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.util.Base64;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

public abstract class Base64EncodedFileFormat {
    protected String begin;
    protected String end;
    private Hashtable<String, String> headers = new Hashtable();
    private int MAX_LINE_LENGTH = 70;

    protected Base64EncodedFileFormat(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }

    public static boolean isFormatted(byte[] formattedKey, String begin, String end) {
        String test = new String(formattedKey);
        return test.indexOf(begin) >= 0 && test.indexOf(end) > 0;
    }

    public void setHeaderValue(String headerTag, String headerValue) {
        this.headers.put(headerTag, headerValue);
    }

    public String getHeaderValue(String headerTag) {
        return this.headers.get(headerTag);
    }

    protected byte[] getKeyBlob(byte[] formattedKey) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(formattedKey)));
        StringBuffer blobBuf = new StringBuffer("");
        do {
            if ((line = reader.readLine()) != null) continue;
            throw new IOException("Incorrect file format!");
        } while (!line.trim().endsWith(this.begin));
        while (true) {
            if ((line = reader.readLine()) == null) {
                throw new IOException("Incorrect file format!");
            }
            int index = (line = line.trim()).indexOf(":");
            if (index <= 0) break;
            while (line.endsWith("\\")) {
                line = line.substring(0, line.length() - 1);
                String tmp = reader.readLine();
                if (tmp == null) {
                    throw new IOException("Incorrect file format!");
                }
                line = line + tmp.trim();
            }
            String headerTag = line.substring(0, index);
            String headerValue = line.substring(index + 1);
            this.headers.put(headerTag, headerValue);
        }
        do {
            blobBuf.append(line);
            line = reader.readLine();
            if (line != null) continue;
            throw new IOException("Invalid file format!");
        } while (!(line = line.trim()).endsWith(this.end));
        return Base64.decode(blobBuf.toString());
    }

    protected byte[] formatKey(byte[] keyblob) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(this.begin.getBytes());
        out.write(10);
        Enumeration<String> e = this.headers.keys();
        while (e.hasMoreElements()) {
            String headerTag = e.nextElement();
            String headerValue = this.headers.get(headerTag);
            String header = headerTag + ": " + headerValue;
            for (int pos = 0; pos < header.length(); pos += this.MAX_LINE_LENGTH) {
                String line = header.substring(pos, pos + this.MAX_LINE_LENGTH < header.length() ? pos + this.MAX_LINE_LENGTH : header.length()) + (pos + this.MAX_LINE_LENGTH < header.length() ? "\\" : "");
                out.write(line.getBytes());
                out.write(10);
            }
        }
        String encoded = Base64.encodeBytes(keyblob, false);
        out.write(encoded.getBytes());
        out.write(10);
        out.write(this.end.getBytes());
        out.write(10);
        return out.toByteArray();
    }
}

