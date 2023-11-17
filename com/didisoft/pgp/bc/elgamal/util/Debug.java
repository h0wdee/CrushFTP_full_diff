/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

import java.io.PrintWriter;

public class Debug {
    public static final boolean GLOBAL_TRACE = true;
    public static final boolean GLOBAL_DEBUG = true;
    public static final boolean GLOBAL_DEBUG_SLOW = false;
    private static final PrintWriter err = new PrintWriter(System.err, true);

    private Debug() {
    }

    public static boolean isTraceable(String string) {
        return false;
    }

    public static int getLevel(String string) {
        return 0;
    }

    public static int getLevel(String string, String string2) {
        int n;
        int n2 = Debug.getLevel(string);
        return n2 > (n = Debug.getLevel(string2)) ? n2 : n;
    }

    public static PrintWriter getOutput() {
        return err;
    }
}

