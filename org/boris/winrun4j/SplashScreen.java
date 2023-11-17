/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import org.boris.winrun4j.NativeHelper;

public class SplashScreen {
    public static long getWindowHandle() {
        return NativeHelper.call(0L, "SplashScreen_GetWindowHandle", new long[0]);
    }

    public static void close() {
        NativeHelper.call(0L, "SplashScreen_Close", new long[0]);
    }

    public static void setText(String text, int x, int y) {
        long ptr = NativeHelper.toNativeString(text, false);
        NativeHelper.call(0L, "SplashScreen_SetText", ptr, x, y);
        NativeHelper.free(ptr);
    }

    public static void setTextFont(String text, int height) {
        long ptr = NativeHelper.toNativeString(text, false);
        NativeHelper.call(0L, "SplashScreen_SetTextFont", ptr, height);
        NativeHelper.free(ptr);
    }

    public static void setTextColor(int r, int g, int b) {
        NativeHelper.call(0L, "SplashScreen_SetTextColor", r, g, b);
    }

    public static void setTextBgColor(int r, int g, int b) {
        NativeHelper.call(0L, "SplashScreen_SetBbColor", r, g, b);
    }
}

