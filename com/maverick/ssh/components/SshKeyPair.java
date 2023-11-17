/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components;

import com.maverick.ssh.components.SshPrivateKey;
import com.maverick.ssh.components.SshPublicKey;

public class SshKeyPair {
    SshPrivateKey privatekey;
    SshPublicKey publickey;

    public SshPrivateKey getPrivateKey() {
        return this.privatekey;
    }

    public SshPublicKey getPublicKey() {
        return this.publickey;
    }

    public static SshKeyPair getKeyPair(SshPrivateKey prv, SshPublicKey pub) {
        SshKeyPair pair = new SshKeyPair();
        pair.publickey = pub;
        pair.privatekey = prv;
        return pair;
    }

    public void setPrivateKey(SshPrivateKey privatekey) {
        this.privatekey = privatekey;
    }

    public void setPublicKey(SshPublicKey publickey) {
        this.publickey = publickey;
    }

    public int hashCode() {
        return this.privatekey.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof SshKeyPair) {
            SshKeyPair other = (SshKeyPair)obj;
            if (other.privatekey != null && other.publickey != null && this.privatekey != null && this.publickey != null) {
                return other.publickey.equals(this.publickey) && other.privatekey.equals(this.privatekey);
            }
        }
        return false;
    }
}

