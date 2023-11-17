/*
 * Decompiled with CFR 0.152.
 */
package jline;

import jline.TerminalSupport;
import jline.internal.Log;

public class OSvTerminal
extends TerminalSupport {
    public Class<?> sttyClass = null;
    public Object stty = null;

    public OSvTerminal() {
        super(true);
        this.setAnsiSupported(true);
        try {
            if (this.stty == null) {
                this.sttyClass = Class.forName("com.cloudius.util.Stty");
                this.stty = this.sttyClass.newInstance();
            }
        }
        catch (Exception e) {
            Log.warn("Failed to load com.cloudius.util.Stty", e);
        }
    }

    public void init() throws Exception {
        super.init();
        if (this.stty != null) {
            this.sttyClass.getMethod("jlineMode", new Class[0]).invoke(this.stty, new Object[0]);
        }
    }

    public void restore() throws Exception {
        if (this.stty != null) {
            this.sttyClass.getMethod("reset", new Class[0]).invoke(this.stty, new Object[0]);
        }
        super.restore();
        System.out.println();
    }
}

