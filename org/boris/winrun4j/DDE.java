/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.util.LinkedHashSet;
import java.util.Set;
import org.boris.winrun4j.ActivationListener;
import org.boris.winrun4j.FileAssociationListener;
import org.boris.winrun4j.NativeHelper;

public class DDE {
    private static Set fileAssociationListeners = new LinkedHashSet();
    private static Set activationListeners = new LinkedHashSet();

    public static void ready() {
        NativeHelper.call(0L, "DDE_Ready", new long[0]);
    }

    public static void addFileAssocationListener(FileAssociationListener listener) {
        fileAssociationListeners.add(listener);
    }

    public static void execute(String command) {
        for (FileAssociationListener listener : fileAssociationListeners) {
            listener.execute(command);
        }
    }

    public static void addActivationListener(ActivationListener listener) {
        activationListeners.add(listener);
    }

    public static void activate(String cmdLine) {
        for (ActivationListener listener : activationListeners) {
            listener.activate(cmdLine);
        }
    }
}

