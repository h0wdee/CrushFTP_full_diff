/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwx;

public class CompactSerializer {
    private static final String PERIOD_SEPARATOR = ".";
    private static final String PERIOD_SEPARATOR_REGEX = "\\.";
    private static final String EMPTY_STRING = "";

    public static String[] deserialize(String compactSerialization) {
        String[] parts = compactSerialization.split(PERIOD_SEPARATOR_REGEX);
        if (compactSerialization.endsWith(PERIOD_SEPARATOR)) {
            String[] tempParts = new String[parts.length + 1];
            System.arraycopy(parts, 0, tempParts, 0, parts.length);
            tempParts[parts.length] = EMPTY_STRING;
            parts = tempParts;
        }
        return parts;
    }

    public static String serialize(String ... parts) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < parts.length) {
            String part = parts[i] == null ? EMPTY_STRING : parts[i];
            sb.append(part);
            if (i != parts.length - 1) {
                sb.append(PERIOD_SEPARATOR);
            }
            ++i;
        }
        return sb.toString();
    }
}

