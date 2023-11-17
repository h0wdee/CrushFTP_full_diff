/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

class IJCE_Properties {
    static final String PRODUCT_NAME = "IJCE";
    static final String LIB_DIRNAME = "ijce-lib";
    static final String[] PROPERTIES_FILES = new String[]{"IJCE.properties"};
    private static final Properties properties = new Properties();
    private static String lib_path;

    IJCE_Properties() {
    }

    static String getLibraryPath() throws IOException {
        if (lib_path == null) {
            throw new IOException("IJCE library directory (ijce-lib) could not be found");
        }
        return lib_path;
    }

    private static void setProperties() {
    }

    static void save(OutputStream outputStream, String string) {
        properties.save(outputStream, string);
    }

    static String getProperty(String string) {
        return properties.getProperty(string);
    }

    static String getProperty(String string, String string2) {
        return properties.getProperty(string, string2);
    }

    static Enumeration propertyNames() {
        return properties.propertyNames();
    }

    static void list(PrintStream printStream) {
        properties.list(printStream);
    }

    static void list(PrintWriter printWriter) {
        properties.list(printWriter);
    }

    static {
        IJCE_Properties.setProperties();
    }
}

