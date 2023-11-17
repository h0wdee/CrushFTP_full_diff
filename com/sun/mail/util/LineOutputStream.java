/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import com.sun.mail.util.ASCIIUtility;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LineOutputStream
extends FilterOutputStream {
    private static byte[] newline = new byte[2];

    public LineOutputStream(OutputStream out) {
        super(out);
    }

    public void writeln(String s) throws IOException {
        byte[] bytes = ASCIIUtility.getBytes(s);
        this.out.write(bytes);
        this.out.write(newline);
    }

    public void writeln() throws IOException {
        this.out.write(newline);
    }

    static {
        LineOutputStream.newline[0] = 13;
        LineOutputStream.newline[1] = 10;
    }
}

