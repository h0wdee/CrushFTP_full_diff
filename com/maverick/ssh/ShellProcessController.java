/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.ssh.ShellController;
import com.maverick.ssh.ShellDefaultMatcher;
import com.maverick.ssh.ShellMatcher;
import com.maverick.ssh.ShellProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellProcessController
extends ShellController {
    ShellProcess process;
    static Logger log = LoggerFactory.getLogger(ShellProcessController.class);

    public ShellProcessController(ShellProcess process) {
        this(process, new ShellDefaultMatcher());
    }

    public ShellProcessController(ShellProcess process, ShellMatcher matcher) {
        super(process.getShell(), matcher, process.getInputStream());
        this.process = process;
    }

    @Override
    public boolean isActive() {
        return this.process.isActive();
    }
}

