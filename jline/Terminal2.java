/*
 * Decompiled with CFR 0.152.
 */
package jline;

import jline.Terminal;

public interface Terminal2
extends Terminal {
    public boolean getBooleanCapability(String var1);

    public Integer getNumericCapability(String var1);

    public String getStringCapability(String var1);
}

