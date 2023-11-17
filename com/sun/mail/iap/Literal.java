/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.iap;

import java.io.IOException;
import java.io.OutputStream;

public interface Literal {
    public int size();

    public void writeTo(OutputStream var1) throws IOException;
}

