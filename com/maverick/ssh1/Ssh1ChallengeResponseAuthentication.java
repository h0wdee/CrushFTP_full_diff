/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh1;

import com.maverick.ssh.SshAuthentication;

public class Ssh1ChallengeResponseAuthentication
implements SshAuthentication {
    String username;
    Prompt prompt;

    @Override
    public String getMethod() {
        return "challenge";
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }

    public Prompt getPrompt() {
        return this.prompt;
    }

    public static interface Prompt {
        public String getResponse(String var1);
    }
}

