/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

public class ShellTimeoutException
extends Exception {
    private static final long serialVersionUID = -7736649465198590395L;

    ShellTimeoutException() {
        super("The shell operation timed out");
    }

    ShellTimeoutException(String str) {
        super(str);
    }
}

