/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh1;

import com.maverick.ssh.SshAuthentication;
import com.maverick.ssh.components.SshPrivateKey;
import com.maverick.ssh.components.SshPublicKey;

public class Ssh1RhostsRsaAuthentication
implements SshAuthentication {
    String username;
    String clientUsername;
    SshPrivateKey prv;
    SshPublicKey pub;

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getMethod() {
        return "rhosts";
    }

    public void setPublicKey(SshPublicKey pub) {
        this.pub = pub;
    }

    public void setPrivateKey(SshPrivateKey prv) {
        this.prv = prv;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public String getClientUsername() {
        return this.clientUsername == null ? this.username : this.clientUsername;
    }

    public SshPrivateKey getPrivateKey() {
        return this.prv;
    }

    public SshPublicKey getPublicKey() {
        return this.pub;
    }
}

