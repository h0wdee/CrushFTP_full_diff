/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import com.didisoft.pgp.bc.elgamal.security.IJCE;
import com.didisoft.pgp.bc.elgamal.security.IJCE_Traceable;
import com.didisoft.pgp.bc.elgamal.security.IllegalBlockSizeException;
import com.didisoft.pgp.bc.elgamal.security.InvalidParameterTypeException;
import com.didisoft.pgp.bc.elgamal.security.NoSuchParameterException;
import com.didisoft.pgp.bc.elgamal.security.Padding;
import com.didisoft.pgp.bc.elgamal.security.Parameterized;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

abstract class PaddingScheme
extends IJCE_Traceable
implements Parameterized,
Padding {
    private String algorithm;
    protected int blockSize;

    protected PaddingScheme(String string) {
        super("PaddingScheme");
        if (string == null) {
            throw new NullPointerException("algorithm == null");
        }
        this.algorithm = string;
    }

    public static PaddingScheme getInstance(String string) throws NoSuchAlgorithmException {
        return (PaddingScheme)IJCE.getImplementation(string, "PaddingScheme");
    }

    public static PaddingScheme getInstance(String string, String string2) throws NoSuchAlgorithmException, NoSuchProviderException {
        return (PaddingScheme)IJCE.getImplementation(string, string2, "PaddingScheme");
    }

    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "PaddingScheme");
    }

    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("PaddingScheme");
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final int getBlockSize() {
        return this.blockSize;
    }

    public final int pad(byte[] byArray, int n, int n2) {
        if (n < 0 || n2 < 0) {
            throw new ArrayIndexOutOfBoundsException("offset < 0 || length < 0");
        }
        int n3 = this.blockSize;
        int n4 = n2 - n2 % n3;
        if ((long)n + (long)n4 + (long)n3 > (long)byArray.length) {
            throw new ArrayIndexOutOfBoundsException("(long)offset + length + padLength(length) > in.length");
        }
        n += n4;
        n2 -= n4;
        if (this.tracing) {
            this.traceMethod("enginePad(<" + byArray + ">, " + n + ", " + n2 + ")");
        }
        int n5 = this.enginePad(byArray, n, n2);
        if (this.tracing) {
            this.traceResult(n5);
        }
        return n5;
    }

    public final int padLength(int n) {
        return this.blockSize - n % this.blockSize;
    }

    public final int unpad(byte[] byArray, int n, int n2) {
        if (n2 == 0) {
            return 0;
        }
        if (n < 0 || n2 < 0 || (long)n + (long)n2 > (long)byArray.length) {
            throw new ArrayIndexOutOfBoundsException("offset < 0 || length < 0 || (long)offset + length > in.length");
        }
        if (this.tracing) {
            this.traceMethod("engineUnpad(<" + byArray + ">, " + n + ", " + n2 + ")");
        }
        int n3 = this.engineUnpad(byArray, n, n2);
        if (this.tracing) {
            this.traceResult(n3);
        }
        return n3;
    }

    public final String paddingScheme() {
        return this.algorithm;
    }

    public void setParameter(String string, Object object) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        if (string == null) {
            throw new NullPointerException("param == null");
        }
        this.engineSetParameter(string, object);
    }

    public Object getParameter(String string) throws NoSuchParameterException, InvalidParameterException {
        if (string == null) {
            throw new NullPointerException("param == null");
        }
        return this.engineGetParameter(string);
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    public String toString() {
        return "PaddingScheme [" + this.getAlgorithm() + "]";
    }

    protected void engineSetBlockSize(int n) {
        if (n < 1 || !this.engineIsValidBlockSize(n)) {
            throw new IllegalBlockSizeException(this.getAlgorithm() + ": " + n + " is not a valid block size");
        }
        this.blockSize = n;
    }

    protected abstract int enginePad(byte[] var1, int var2, int var3);

    protected abstract int engineUnpad(byte[] var1, int var2, int var3);

    protected boolean engineIsValidBlockSize(int n) {
        return true;
    }

    protected void engineSetParameter(String string, Object object) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        throw new NoSuchParameterException(this.getAlgorithm() + ": " + string);
    }

    protected Object engineGetParameter(String string) throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(this.getAlgorithm() + ": " + string);
    }
}

