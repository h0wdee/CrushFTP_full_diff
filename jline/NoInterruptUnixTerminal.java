/*
 * Decompiled with CFR 0.152.
 */
package jline;

import jline.UnixTerminal;

public class NoInterruptUnixTerminal
extends UnixTerminal {
    private String intr;

    public void init() throws Exception {
        super.init();
        this.intr = this.getSettings().getPropertyAsString("intr");
        if ("<undef>".equals(this.intr)) {
            this.intr = null;
        }
        if (this.intr != null) {
            this.getSettings().undef("intr");
        }
    }

    public void restore() throws Exception {
        if (this.intr != null) {
            this.getSettings().set("intr", this.intr);
        }
        super.restore();
    }
}

