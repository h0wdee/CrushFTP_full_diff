/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

public class HexBuilder {
    public static StringBuilder toHex(byte[] byteArr) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArr) {
            hexString.append(String.format("%02X ", b));
        }
        return hexString;
    }

    public static StringBuilder toHex(byte[] byteArr, int sz) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < sz; ++i) {
            hexString.append(String.format("%02x ", byteArr[i]));
        }
        return hexString;
    }

    public static StringBuilder toHex(byte[] byteArr, int offset, int sz) {
        StringBuilder hexString = new StringBuilder();
        for (int i = offset; i < sz; ++i) {
            hexString.append(String.format("%02x ", byteArr[i]));
        }
        return hexString;
    }
}

