/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshAuthentication;

public class PasswordAuthentication
implements SshAuthentication {
    String password;
    String username;

    public PasswordAuthentication() {
    }

    public PasswordAuthentication(String password) {
        this.setPassword(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public String getMethod() {
        return "password";
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}

