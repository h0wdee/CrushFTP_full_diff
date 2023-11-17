/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import com.didisoft.pgp.bc.elgamal.security.IJCE_Properties;
import com.didisoft.pgp.bc.elgamal.security.IJCE_Traceable;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class IJCE {
    private static final boolean DEBUG = true;
    private static final int debuglevel = IJCE.getDebugLevel("IJCE");
    private static final PrintWriter err = new PrintWriter(System.err, true);
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;
    private static final int INTER_VERSION = 0;
    private static final boolean IS_SNAPSHOT = true;
    private static final String CVS_DATE = "$Date: 2000/08/17 11:35:24 $";
    private static Hashtable typeToClass = new Hashtable();

    private IJCE() {
    }

    public static String[] getAlgorithms(Provider provider, String string) {
        Object object;
        if (IJCE.getClassForType(string) == null) {
            return new String[0];
        }
        String string2 = string + ".";
        Vector<String> vector = new Vector<String>();
        Enumeration<?> enumeration = provider.propertyNames();
        while (enumeration.hasMoreElements()) {
            object = (String)enumeration.nextElement();
            if (!object.startsWith(string2)) continue;
            vector.addElement(object.substring(string2.length()));
        }
        object = new String[vector.size()];
        vector.copyInto((Object[])object);
        return object;
    }

    public static String[] getAlgorithms(String string) {
        Enumeration<Object> enumeration;
        if (IJCE.getClassForType(string) == null) {
            return new String[0];
        }
        String string2 = string + ".";
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        if (string.equals("PaddingScheme")) {
            hashtable.put("NONE", "");
        } else if (string.equals("Mode")) {
            hashtable.put("ECB", "");
        }
        Provider[] providerArray = IJCE.getProvidersInternal();
        for (int i = 0; i < providerArray.length; ++i) {
            enumeration = providerArray[i].propertyNames();
            while (enumeration.hasMoreElements()) {
                String string3 = (String)enumeration.nextElement();
                if (!string3.startsWith(string2)) continue;
                hashtable.put(string3.substring(string2.length()), "");
            }
        }
        String[] stringArray = new String[hashtable.size()];
        enumeration = hashtable.keys();
        int n = 0;
        while (enumeration.hasMoreElements()) {
            stringArray[n++] = (String)enumeration.nextElement();
        }
        return stringArray;
    }

    public static boolean enableTracing(Object object, PrintWriter printWriter) {
        if (object instanceof IJCE_Traceable) {
            ((IJCE_Traceable)object).enableTracing(printWriter);
            return true;
        }
        return false;
    }

    public static boolean enableTracing(Object object) {
        return IJCE.enableTracing(object, err);
    }

    public static void disableTracing(Object object) {
        if (object instanceof IJCE_Traceable) {
            ((IJCE_Traceable)object).disableTracing();
        }
    }

    public static String getStandardName(String string, String string2) {
        String string3 = "Alias." + string2;
        String string4 = Security.getAlgorithmProperty(string, string3);
        return string4 != null ? string4 : string;
    }

    public static Object getImplementation(String string, String string2) throws NoSuchAlgorithmException {
        try {
            return IJCE.getImplementation(string, null, string2);
        }
        catch (NoSuchProviderException noSuchProviderException) {
            throw new NoSuchAlgorithmException(noSuchProviderException.getMessage());
        }
    }

    public static Object getImplementation(String string, String string2, String string3) throws NoSuchAlgorithmException, NoSuchProviderException {
        String string4;
        Class clazz = IJCE.getImplementationClass(string, string2, string3);
        try {
            return clazz.newInstance();
        }
        catch (LinkageError linkageError) {
            string4 = " could not be linked correctly.\n" + linkageError;
        }
        catch (InstantiationException instantiationException) {
            string4 = " cannot be instantiated.\n" + instantiationException;
        }
        catch (IllegalAccessException illegalAccessException) {
            string4 = " cannot be accessed.\n" + illegalAccessException;
        }
        throw new NoSuchAlgorithmException("class configured for " + string3 + ": " + clazz.getName() + string4);
    }

    public static Class getImplementationClass(String string, String string2) throws NoSuchAlgorithmException {
        try {
            return IJCE.getImplementationClass(string, null, string2);
        }
        catch (NoSuchProviderException noSuchProviderException) {
            throw new NoSuchAlgorithmException(noSuchProviderException.getMessage());
        }
    }

    public static Class getImplementationClass(String string, String string2, String string3) throws NoSuchAlgorithmException, NoSuchProviderException {
        String string4 = IJCE.getStandardName(string, string3);
        Class clazz = IJCE.getClassForType(string3);
        if (clazz == null) {
            throw new NoSuchAlgorithmException(string3 + " is not a configured type");
        }
        Class clazz2 = IJCE.getClassCandidate(string4, string2, string3);
        if (clazz.isAssignableFrom(clazz2)) {
            return clazz2;
        }
        throw new NoSuchAlgorithmException("class configured for " + string3 + ": " + clazz2.getName() + " is not a subclass of " + clazz.getName());
    }

    private static Class getClassCandidate(String string, String string2, String string3) throws NoSuchAlgorithmException, NoSuchProviderException {
        Class clazz;
        String string4 = string3 + "." + string;
        if (string2 == null) {
            Provider[] providerArray = IJCE.getProvidersInternal();
            for (int i = 0; i < providerArray.length; ++i) {
                String string5 = providerArray[i].getProperty(string4);
                if (string5 == null) continue;
                try {
                    Class clazz2 = IJCE.findEngineClass(string5, string3);
                    if (clazz2 == null) continue;
                    return clazz2;
                }
                catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                    // empty catch block
                }
            }
            throw new NoSuchAlgorithmException("algorithm " + string + " is not available.");
        }
        Provider provider = IJCE.getProviderInternal(string2);
        if (provider == null) {
            throw new NoSuchProviderException("provider " + string2 + " is not available.");
        }
        String string6 = provider.getProperty(string4);
        if (string6 != null && (clazz = IJCE.findEngineClass(string6, string3)) != null) {
            return clazz;
        }
        throw new NoSuchAlgorithmException("algorithm " + string + " is not available from provider " + string2);
    }

    private static Class findEngineClass(String string, String string2) throws NoSuchAlgorithmException {
        String string3;
        try {
            return Class.forName(string);
        }
        catch (ClassNotFoundException classNotFoundException) {
            return null;
        }
        catch (NoSuchMethodError noSuchMethodError) {
            string3 = " does not have a zero-argument constructor.\n" + noSuchMethodError;
        }
        catch (LinkageError linkageError) {
            string3 = " could not be linked correctly.\n" + linkageError;
        }
        throw new NoSuchAlgorithmException("class configured for " + string2 + ": " + string + string3);
    }

    public static int getMajorVersion() {
        return 1;
    }

    public static int getMinorVersion() {
        return 1;
    }

    public static int getIntermediateVersion() {
        return 0;
    }

    public static boolean isVersionAtLeast(int n, int n2, int n3) {
        if (1 > n) {
            return true;
        }
        if (1 < n) {
            return false;
        }
        if (1 > n2) {
            return true;
        }
        if (1 < n2) {
            return false;
        }
        return 0 >= n3;
    }

    public static String getReleaseDate() {
        try {
            return CVS_DATE.substring(7, 17);
        }
        catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
            return "unknown";
        }
    }

    public static String getVersionString() {
        StringBuffer stringBuffer = new StringBuffer("IJCE ").append(1).append(".").append(1);
        stringBuffer.append(" (").append(IJCE.getReleaseDate()).append(" snapshot)");
        return stringBuffer.toString();
    }

    public static boolean isProvidingJCA() {
        return true;
    }

    public static boolean isProvidingJCE() {
        return true;
    }

    private static Class getClassForType(String string) {
        Class<?> clazz = (Class<?>)typeToClass.get(string);
        if (clazz != null) {
            return clazz;
        }
        String string2 = IJCE_Properties.getProperty("Type." + string);
        if (string2 == null) {
            return null;
        }
        try {
            clazz = Class.forName(string2);
        }
        catch (LinkageError linkageError) {
            IJCE.debug("Error loading class for algorithm type " + string + ": " + linkageError);
            return null;
        }
        catch (ClassNotFoundException classNotFoundException) {
            IJCE.debug("Error loading class for algorithm type " + string + ": " + classNotFoundException);
            return null;
        }
        typeToClass.put(string, clazz);
        return clazz;
    }

    private static Provider[] getProvidersInternal() {
        Provider[] providerArray = Security.getProviders();
        return providerArray;
    }

    private static Provider getProviderInternal(String string) {
        Provider provider = Security.getProvider(string);
        return provider;
    }

    static void debug(String string) {
        err.println(string);
    }

    static void error(String string) {
        err.println(string);
    }

    static void reportBug(String string) {
        err.println("\n" + string + "\n\nPlease report this as a bug to <david.hopwood@lmh.ox.ac.uk>, including\nany other messages displayed on the console, and a description of what\nappeared to cause the error.\n");
        throw new InternalError(string);
    }

    static void listProviders() {
        Provider[] providerArray = IJCE.getProvidersInternal();
        for (int i = 0; i < providerArray.length; ++i) {
            err.println("providers[" + i + "] = " + providerArray[i]);
        }
    }

    static int getDebugLevel(String string) {
        String string2 = IJCE_Properties.getProperty("Debug.Level." + string);
        if (string2 == null && (string2 = IJCE_Properties.getProperty("Debug.Level.*")) == null) {
            return 0;
        }
        try {
            return Integer.parseInt(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    static PrintWriter getDebugOutput() {
        return err;
    }

    public static void main(String[] stringArray) {
        System.out.println(IJCE.getVersionString());
        System.out.println();
        IJCE.listProviders();
        System.out.println();
        try {
            String string = IJCE_Properties.getLibraryPath();
            System.out.println("The library directory is");
            System.out.println("  " + string);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }
}

