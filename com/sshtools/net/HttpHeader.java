/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

public abstract class HttpHeader {
    protected static final String white_SPACE = " \t\r";
    Hashtable<String, String> fields = new Hashtable();
    protected String begin;

    protected HttpHeader() {
    }

    protected String readLine(InputStream in) throws IOException {
        StringBuffer lineBuf = new StringBuffer();
        while (true) {
            int c;
            if ((c = in.read()) == -1) {
                throw new IOException("Failed to read expected HTTP header line");
            }
            if (c == 10) continue;
            if (c == 13) break;
            lineBuf.append((char)c);
        }
        return new String(lineBuf);
    }

    public String getStartLine() {
        return this.begin;
    }

    public Hashtable<String, String> getHeaderFields() {
        return this.fields;
    }

    public Enumeration<String> getHeaderFieldNames() {
        return this.fields.keys();
    }

    public String getHeaderField(String headerName) {
        Enumeration<String> e = this.fields.keys();
        while (e.hasMoreElements()) {
            String f = e.nextElement();
            if (!f.equalsIgnoreCase(headerName)) continue;
            return this.fields.get(f);
        }
        return null;
    }

    public void setHeaderField(String headerName, String value) {
        this.fields.put(headerName, value);
    }

    public String toString() {
        String str = this.begin + "\r\n";
        Enumeration<String> it = this.getHeaderFieldNames();
        while (it.hasMoreElements()) {
            String fieldName = it.nextElement();
            str = str + fieldName + ": " + this.getHeaderField(fieldName) + "\r\n";
        }
        str = str + "\r\n";
        return str;
    }

    protected void processHeaderFields(InputStream in) throws IOException {
        int c;
        this.fields = new Hashtable();
        StringBuffer lineBuf = new StringBuffer();
        String lastHeaderName = null;
        while (true) {
            if ((c = in.read()) == -1) {
                throw new IOException("EOF returned from server but HTTP response is not complete!");
            }
            if (c == 10) continue;
            if (c != 13) {
                lineBuf.append((char)c);
                continue;
            }
            if (lineBuf.length() == 0) break;
            String line = lineBuf.toString();
            lastHeaderName = this.processNextLine(line, lastHeaderName);
            lineBuf.setLength(0);
        }
        c = in.read();
    }

    private String processNextLine(String line, String lastHeaderName) throws IOException {
        String value;
        String name;
        char c = line.charAt(0);
        if (c == ' ' || c == '\t') {
            name = lastHeaderName;
            value = this.getHeaderField(lastHeaderName) + " " + line.trim();
        } else {
            int n = line.indexOf(58);
            if (n == -1) {
                throw new IOException("HTTP Header encoutered a corrupt field: '" + line + "'");
            }
            name = line.substring(0, n).toLowerCase();
            value = line.substring(n + 1).trim();
        }
        this.setHeaderField(name, value);
        return name;
    }
}

