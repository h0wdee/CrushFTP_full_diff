/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.sshtools.net.CIDRNetwork;
import java.util.Arrays;
import java.util.Collection;

public class Patterns {
    public static boolean matchesWithCIDR(Collection<String> patterns, String value) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        boolean positiveMatch = false;
        boolean negativeMatch = false;
        for (String pattern : patterns) {
            if (pattern.startsWith("!") && !negativeMatch) {
                if (pattern.contains("/")) {
                    negativeMatch = Patterns.cidrMatch(value, pattern);
                    continue;
                }
                if (value.contains("/") && !pattern.contains("/")) {
                    negativeMatch = Patterns.cidrMatch(pattern, value);
                    continue;
                }
                if (!value.matches(Patterns.convertToRegex(pattern.substring(1)))) continue;
                negativeMatch = true;
                continue;
            }
            if (positiveMatch) continue;
            if (pattern.contains("/")) {
                positiveMatch = Patterns.cidrMatch(value, pattern);
                continue;
            }
            if (value.contains("/") && !pattern.contains("/")) {
                positiveMatch = Patterns.cidrMatch(pattern, value);
                continue;
            }
            if (!value.matches(Patterns.convertToRegex(pattern))) continue;
            positiveMatch = true;
        }
        return !negativeMatch && positiveMatch;
    }

    public static boolean matches(Collection<String> patterns, String value) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        boolean positiveMatch = false;
        boolean negativeMatch = false;
        for (String pattern : patterns) {
            if (pattern.startsWith("!") && !negativeMatch) {
                if (!value.matches(Patterns.convertToRegex(pattern.substring(1)))) continue;
                negativeMatch = true;
                continue;
            }
            if (positiveMatch || !value.matches(Patterns.convertToRegex(pattern))) continue;
            positiveMatch = true;
        }
        return !negativeMatch && positiveMatch;
    }

    static String convertToRegex(String pattern) {
        pattern = pattern.replace(".", "\\.");
        pattern = pattern.replace("*", ".*");
        pattern = pattern.replace("?", ".");
        return pattern;
    }

    private static boolean cidrMatch(String value, String pattern) {
        CIDRNetwork cidr = new CIDRNetwork(pattern);
        return cidr.isValidAddressForNetwork(value);
    }

    public static void main(String[] args) {
        System.out.println("Expecting true: " + Patterns.matchesWithCIDR(Arrays.asList("!*.dialup.example.com", "*.example.com"), "foo.example.com"));
        System.out.println("Expecting false: " + Patterns.matchesWithCIDR(Arrays.asList("!*.dialup.example.com", "*.example.com"), "foo.dialup.example.com"));
        System.out.println("Expecting false: " + Patterns.matchesWithCIDR(Arrays.asList("!*.dialup.example.com", "*.example.com"), "foo.com"));
    }
}

