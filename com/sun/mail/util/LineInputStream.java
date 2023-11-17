/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class LineInputStream
extends FilterInputStream {
    private char[] lineBuffer = null;
    private static int MAX_INCR = 0x100000;

    public LineInputStream(InputStream in) {
        super(in);
    }

    public String readLine() throws IOException {
        int c1;
        char[] buf = this.lineBuffer;
        if (buf == null) {
            buf = this.lineBuffer = new char[128];
        }
        int room = buf.length;
        int offset = 0;
        while ((c1 = this.in.read()) != -1 && c1 != 10) {
            if (c1 == 13) {
                int c2;
                boolean twoCRs = false;
                if (this.in.markSupported()) {
                    this.in.mark(2);
                }
                if ((c2 = this.in.read()) == 13) {
                    twoCRs = true;
                    c2 = this.in.read();
                }
                if (c2 == 10) break;
                if (this.in.markSupported()) {
                    this.in.reset();
                    break;
                }
                if (!(this.in instanceof PushbackInputStream)) {
                    this.in = new PushbackInputStream(this.in, 2);
                }
                if (c2 != -1) {
                    ((PushbackInputStream)this.in).unread(c2);
                }
                if (!twoCRs) break;
                ((PushbackInputStream)this.in).unread(13);
                break;
            }
            if (--room < 0) {
                buf = buf.length < MAX_INCR ? new char[buf.length * 2] : new char[buf.length + MAX_INCR];
                room = buf.length - offset - 1;
                System.arraycopy(this.lineBuffer, 0, buf, 0, offset);
                this.lineBuffer = buf;
            }
            buf[offset++] = (char)c1;
        }
        if (c1 == -1 && offset == 0) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }
}

