/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import com.didisoft.pgp.bc.elgamal.security.IJCE;
import com.didisoft.pgp.bc.elgamal.security.IJCE_Properties;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

abstract class IJCE_Traceable {
    boolean tracing;
    private PrintWriter out;
    private static int indent;
    private static boolean dangling;
    private static Hashtable traced;

    IJCE_Traceable(String string) {
        PrintWriter printWriter = (PrintWriter)traced.get(this.getClass().getName());
        if (printWriter == null) {
            printWriter = (PrintWriter)traced.get(string);
        }
        if (printWriter != null) {
            this.enableTracing(printWriter);
        }
    }

    void enableTracing(PrintWriter printWriter) {
        if (printWriter == null) {
            throw new NullPointerException("out == null");
        }
        this.out = printWriter;
        this.tracing = true;
    }

    void disableTracing() {
        this.tracing = false;
        this.out = null;
    }

    void traceVoidMethod(String string) {
        try {
            this.newline();
            this.out.println("<" + this + ">." + string);
            dangling = false;
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    void traceMethod(String string) {
        try {
            this.newline();
            this.out.print("<" + this + ">." + string + " ");
            this.out.flush();
            dangling = true;
            ++indent;
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    void traceResult(String string) {
        try {
            if (!dangling) {
                for (int i = 1; i < indent; ++i) {
                    this.out.print("    ");
                }
                this.out.print("... ");
            }
            this.out.println("= " + string);
            dangling = false;
            --indent;
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    void traceResult(int n) {
        this.traceResult(Integer.toString(n));
    }

    private void newline() {
        if (dangling) {
            this.out.println("...");
        }
        for (int i = 0; i < indent; ++i) {
            this.out.print("    ");
        }
    }

    static {
        traced = new Hashtable();
        String string = "Trace.";
        int n = string.length();
        PrintWriter printWriter = IJCE.getDebugOutput();
        Enumeration enumeration = IJCE_Properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String string2;
            String string3 = (String)enumeration.nextElement();
            if (!string3.startsWith(string) || (string2 = IJCE_Properties.getProperty(string3)) == null || !string2.equalsIgnoreCase("true")) continue;
            traced.put(string3.substring(n), printWriter);
        }
    }
}

