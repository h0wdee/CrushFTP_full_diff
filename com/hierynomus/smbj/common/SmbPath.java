/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.common;

import com.hierynomus.protocol.commons.Objects;
import com.hierynomus.utils.Strings;

public class SmbPath {
    private final String hostname;
    private final String shareName;
    private final String path;

    public SmbPath(String hostname) {
        this(hostname, null, null);
    }

    public SmbPath(String hostname, String shareName) {
        this(hostname, shareName, null);
    }

    public SmbPath(String hostname, String shareName, String path) {
        this.shareName = shareName;
        this.hostname = hostname;
        this.path = SmbPath.rewritePath(path);
    }

    private static String rewritePath(String path) {
        return Strings.isNotBlank(path) ? path.replace('/', '\\') : path;
    }

    public SmbPath(SmbPath parent, String path) {
        this.hostname = parent.hostname;
        if (!Strings.isNotBlank(parent.shareName)) {
            throw new IllegalArgumentException("Can only make child SmbPath of fully specified SmbPath");
        }
        this.shareName = parent.shareName;
        this.path = Strings.isNotBlank(parent.path) ? parent.path + "\\" + SmbPath.rewritePath(path) : SmbPath.rewritePath(path);
    }

    public String toUncPath() {
        StringBuilder b = new StringBuilder("\\\\");
        b.append(this.hostname);
        if (this.shareName != null && !this.shareName.isEmpty()) {
            if (this.shareName.charAt(0) != '\\') {
                b.append("\\");
            }
            b.append(this.shareName);
            if (Strings.isNotBlank(this.path)) {
                b.append("\\").append(this.path);
            }
        }
        return b.toString();
    }

    public String toString() {
        return this.toUncPath();
    }

    public static SmbPath parse(String path) {
        String[] split;
        String rewritten;
        String splitPath = rewritten = SmbPath.rewritePath(path);
        if (rewritten.charAt(0) == '\\') {
            splitPath = rewritten.charAt(1) == '\\' ? rewritten.substring(2) : rewritten.substring(1);
        }
        if ((split = splitPath.split("\\\\", 3)).length == 1) {
            return new SmbPath(split[0]);
        }
        if (split.length == 2) {
            return new SmbPath(split[0], split[1]);
        }
        return new SmbPath(split[0], split[1], split[2]);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SmbPath smbPath = (SmbPath)o;
        return Objects.equals(this.hostname, smbPath.hostname) && Objects.equals(this.shareName, smbPath.shareName) && Objects.equals(this.path, smbPath.path);
    }

    public int hashCode() {
        return Objects.hash(this.hostname, this.shareName, this.path);
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getShareName() {
        return this.shareName;
    }

    public String getPath() {
        return this.path;
    }

    public boolean isOnSameHost(SmbPath other) {
        return other != null && Objects.equals(this.hostname, other.hostname);
    }

    public boolean isOnSameShare(SmbPath other) {
        return this.isOnSameHost(other) && Objects.equals(this.shareName, other.shareName);
    }
}

