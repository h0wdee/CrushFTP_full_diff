/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Validator {
    private static Set<Byte> BAD_CHARACTORS = null;

    public static boolean validDomain(String domain) {
        if (null == domain || domain.length() == 0) {
            return true;
        }
        return Validator.checkNamingConvention(domain);
    }

    public static boolean validServer(String serverName) {
        if (null == serverName || serverName.length() == 0) {
            return false;
        }
        return Validator.checkNamingConvention(serverName);
    }

    private static boolean checkNamingConvention(String name) {
        byte[] arr$ = name.getBytes();
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; ++i$) {
            Byte c = arr$[i$];
            if (!BAD_CHARACTORS.contains(c)) continue;
            return false;
        }
        if (name.equals(".")) {
            return true;
        }
        return !name.startsWith(".");
    }

    public static boolean validName(String name) {
        return null != name && name.length() != 0;
    }

    static {
        BAD_CHARACTORS = new HashSet<Byte>(Arrays.asList((byte)92, (byte)47, (byte)58, (byte)42, (byte)63, (byte)34, (byte)60, (byte)62, (byte)124));
    }
}

