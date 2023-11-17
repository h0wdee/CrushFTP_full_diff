/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ShellMatcher;

public class ShellDefaultMatcher
implements ShellMatcher {
    @Override
    public ShellMatcher.Continue matches(String line, String pattern) {
        boolean match = false;
        for (int i = 0; i < line.length(); ++i) {
            match = line.charAt(i) == pattern.charAt(i);
        }
        if (match && pattern.length() == line.length()) {
            return ShellMatcher.Continue.CONTENT_MATCHES;
        }
        if (match) {
            return ShellMatcher.Continue.MORE_CONTENT_NEEDED;
        }
        return ShellMatcher.Continue.CONTENT_DOES_NOT_MATCH;
    }
}

