/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.iap;

import com.sun.mail.iap.ByteArray;
import com.sun.mail.util.ASCIIUtility;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResponseInputStream {
    private static final int minIncrement = 256;
    private static final int maxIncrement = 262144;
    private static final int incrementSlop = 16;
    private BufferedInputStream bin;

    public ResponseInputStream(InputStream in) {
        this.bin = new BufferedInputStream(in, 2048);
    }

    public ByteArray readResponse() throws IOException {
        return this.readResponse(null);
    }

    /*
     * Unable to fully structure code
     */
    public ByteArray readResponse(ByteArray ba) throws IOException {
        if (ba == null) {
            ba = new ByteArray(new byte[128], 0, 128);
        }
        buffer = ba.getBytes();
        idx = 0;
        block2: while (true) lbl-1000:
        // 3 sources

        {
            b = 0;
            gotCRLF = false;
            while (!gotCRLF && (b = this.bin.read()) != -1) {
                if (b == 10 && idx > 0 && buffer[idx - 1] == 13) {
                    gotCRLF = true;
                }
                if (idx >= buffer.length) {
                    incr = buffer.length;
                    if (incr > 262144) {
                        incr = 262144;
                    }
                    ba.grow(incr);
                    buffer = ba.getBytes();
                }
                buffer[idx++] = (byte)b;
            }
            if (b == -1) {
                throw new IOException("Connection dropped by server?");
            }
            if (idx < 5 || buffer[idx - 3] != 125) break;
            for (i = idx - 4; i >= 0 && buffer[i] != 123; --i) {
            }
            if (i < 0) break;
            count = 0;
            try {
                count = ASCIIUtility.parseInt(buffer, i + 1, idx - 3);
            }
            catch (NumberFormatException e) {
                break;
            }
            if (count <= 0) ** GOTO lbl-1000
            avail = buffer.length - idx;
            if (count + 16 > avail) {
                ba.grow(256 > count + 16 - avail ? 256 : count + 16 - avail);
                buffer = ba.getBytes();
            }
            while (true) {
                if (count <= 0) continue block2;
                actual = this.bin.read(buffer, idx, count);
                if (actual == -1) {
                    throw new IOException("Connection dropped by server?");
                }
                count -= actual;
                idx += actual;
            }
            break;
        }
        ba.setCount(idx);
        return ba;
    }

    public int available() throws IOException {
        return this.bin.available();
    }
}

