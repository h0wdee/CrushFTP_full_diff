/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import com.didisoft.pgp.bc.elgamal.security.IJCE;
import java.io.PrintWriter;
import java.util.Hashtable;

class IJCE_SecuritySupport {
    private static final boolean DEBUG = true;
    private static int debuglevel = IJCE.getDebugLevel("IJCE_SecuritySupport");
    private static PrintWriter err = IJCE.getDebugOutput();
    private static final String TARGET_HELP_FILENAME = "TargetHelp.html";
    private static String targetHelpURL;
    private static Hashtable targets;

    private IJCE_SecuritySupport() {
    }

    private static void debug(String string) {
        err.println("IJCE_SecuritySupport: " + string);
    }

    private static void registerTargets() {
        if (debuglevel >= 4) {
            IJCE_SecuritySupport.debug("Initializing...");
        }
    }

    static void checkPrivilegeEnabled(String string, int n) {
    }

    static void checkSystemCaller(int n) {
    }

    static {
        targets = new Hashtable();
        targetHelpURL = TARGET_HELP_FILENAME;
        try {
            IJCE_SecuritySupport.registerTargets();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            IJCE.reportBug("Unexpected exception in IJCE_SecuritySupport.registerTargets()");
        }
    }
}

