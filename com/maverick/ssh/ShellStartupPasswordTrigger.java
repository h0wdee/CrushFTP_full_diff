/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.ShellDefaultMatcher;
import com.maverick.ssh.ShellMatcher;
import com.maverick.ssh.ShellStartupTrigger;
import com.maverick.ssh.ShellWriter;
import java.io.IOException;

public class ShellStartupPasswordTrigger
implements ShellStartupTrigger {
    String passwordPromptExpression;
    String password;
    ShellMatcher matcher;

    public ShellStartupPasswordTrigger(String passwordPromptExpression, String password) {
        this(passwordPromptExpression, password, new ShellDefaultMatcher());
    }

    public ShellStartupPasswordTrigger(String passwordPromptExpression, String password, ShellMatcher matcher) {
        this.passwordPromptExpression = passwordPromptExpression;
        this.password = password;
        this.matcher = matcher;
    }

    @Override
    public boolean canStartShell(String currentLine, ShellWriter writer) throws IOException {
        switch (this.matcher.matches(currentLine, this.passwordPromptExpression)) {
            case CONTENT_DOES_NOT_MATCH: {
                throw new IOException("Expected password prompt but content does not match");
            }
            case CONTENT_MATCHES: {
                writer.typeAndReturn(this.password);
                return true;
            }
        }
        return false;
    }
}

