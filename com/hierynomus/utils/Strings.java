/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.utils;

import java.util.ArrayList;
import java.util.List;

public class Strings {
    public static List<String> split(String string, char c) {
        int next;
        ArrayList<String> parts = new ArrayList<String>();
        int off = 0;
        while ((next = string.indexOf(c, off)) != -1) {
            parts.add(string.substring(off, next));
            off = next + 1;
        }
        parts.add(string.substring(off));
        return parts;
    }

    public static String join(List<String> strings, char c) {
        StringBuilder joiner = new StringBuilder();
        for (int i = 0; i < strings.size(); ++i) {
            if (i > 0) {
                joiner.append(c);
            }
            joiner.append(strings.get(i));
        }
        return joiner.toString();
    }

    public static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}

