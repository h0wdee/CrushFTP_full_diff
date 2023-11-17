/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.util;

import com.sun.mail.util.QPEncoderStream;
import java.io.IOException;
import java.io.OutputStream;

public class QEncoderStream
extends QPEncoderStream {
    private String specials;
    private static String WORD_SPECIALS = "=_?\"#$%&'(),.:;<>@[\\]^`{|}~";
    private static String TEXT_SPECIALS = "=_?";

    public QEncoderStream(OutputStream out, boolean encodingWord) {
        super(out, Integer.MAX_VALUE);
        this.specials = encodingWord ? WORD_SPECIALS : TEXT_SPECIALS;
    }

    public void write(int c) throws IOException {
        if ((c &= 0xFF) == 32) {
            this.output(95, false);
        } else if (c < 32 || c >= 127 || this.specials.indexOf(c) >= 0) {
            this.output(c, true);
        } else {
            this.output(c, false);
        }
    }

    public static int encodedLength(byte[] b, boolean encodingWord) {
        int len = 0;
        String specials = encodingWord ? WORD_SPECIALS : TEXT_SPECIALS;
        for (int i = 0; i < b.length; ++i) {
            int c = b[i] & 0xFF;
            if (c < 32 || c >= 127 || specials.indexOf(c) >= 0) {
                len += 3;
                continue;
            }
            ++len;
        }
        return len;
    }
}

