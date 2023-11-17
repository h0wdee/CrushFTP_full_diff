/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

public class KBIPrompt {
    private String prompt;
    private String response;
    private boolean echo;

    public KBIPrompt(String prompt, boolean echo) {
        this.prompt = prompt;
        this.echo = echo;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public boolean echo() {
        return this.echo;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return this.response;
    }
}

