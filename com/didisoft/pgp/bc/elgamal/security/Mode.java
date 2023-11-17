/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import com.didisoft.pgp.bc.elgamal.security.Cipher;
import com.didisoft.pgp.bc.elgamal.security.IJCE;
import com.didisoft.pgp.bc.elgamal.security.InvalidParameterTypeException;
import com.didisoft.pgp.bc.elgamal.security.NoSuchParameterException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

abstract class Mode
extends Cipher {
    protected Cipher cipher;

    protected Mode(boolean bl, boolean bl2, String string) {
        super(bl, bl2, string);
    }

    public static Cipher getInstance(String string) throws NoSuchAlgorithmException {
        return (Cipher)IJCE.getImplementation(string, "Mode");
    }

    public static Cipher getInstance(String string, String string2) throws NoSuchAlgorithmException, NoSuchProviderException {
        return (Cipher)IJCE.getImplementation(string, string2, "Mode");
    }

    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "Mode");
    }

    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("Mode");
    }

    public String toString() {
        return "Mode [" + this.getProvider() + " " + this.getAlgorithm() + "/" + this.getMode() + "/" + this.getPadding() + "]";
    }

    protected void engineSetCipher(Cipher cipher) {
        if (cipher == null) {
            throw new NullPointerException("cipher == null");
        }
        this.cipher = cipher;
    }

    protected void engineSetParameter(String string, Object object) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        this.cipher.setParameter(string, object);
    }

    protected Object engineGetParameter(String string) throws NoSuchParameterException, InvalidParameterException {
        return this.cipher.getParameter(string);
    }
}

