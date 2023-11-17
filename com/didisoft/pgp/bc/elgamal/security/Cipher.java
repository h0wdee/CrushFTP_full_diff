/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.security;

import com.didisoft.pgp.bc.elgamal.security.IJCE;
import com.didisoft.pgp.bc.elgamal.security.IJCE_Traceable;
import com.didisoft.pgp.bc.elgamal.security.IllegalBlockSizeException;
import com.didisoft.pgp.bc.elgamal.security.InvalidParameterTypeException;
import com.didisoft.pgp.bc.elgamal.security.Mode;
import com.didisoft.pgp.bc.elgamal.security.NoSuchParameterException;
import com.didisoft.pgp.bc.elgamal.security.PaddingScheme;
import com.didisoft.pgp.bc.elgamal.security.Parameterized;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

public abstract class Cipher
extends IJCE_Traceable
implements Parameterized {
    private static final boolean DEBUG = true;
    private static int debuglevel = IJCE.getDebugLevel("Cipher");
    private static PrintWriter err = IJCE.getDebugOutput();
    public static final int UNINITIALIZED = 0;
    public static final int ENCRYPT = 1;
    public static final int DECRYPT = 2;
    private boolean implBuffering;
    private byte[] buffer;
    private int buffered;
    private int inputSize;
    private int outputSize;
    private String provider;
    private String cipherName;
    private String modeName;
    private String paddingName;
    private PaddingScheme padding;
    private int state;

    private static void debug(String string) {
        err.println("Cipher: " + string);
    }

    private static String dump(byte[] byArray) {
        if (byArray == null) {
            return "null";
        }
        return byArray.toString();
    }

    protected Cipher() {
        super("Cipher");
    }

    protected Cipher(boolean bl, boolean bl2, String string) {
        super("Cipher");
        if (bl2) {
            throw new IllegalArgumentException("IJCE does not support ciphers for which implPadding == true");
        }
        this.implBuffering = bl;
        this.provider = string;
    }

    protected Cipher(boolean bl, String string, String string2) {
        super("Cipher");
        this.implBuffering = bl;
        this.provider = string;
        this.parseAlgorithm(string2);
    }

    private void parseAlgorithm(String string) {
        int n = string.indexOf(47);
        if (n == -1) {
            this.cipherName = string;
        } else {
            this.cipherName = string.substring(0, n);
            int n2 = string.indexOf(47, n + 1);
            if (n2 == -1) {
                this.modeName = string.substring(n + 1);
            } else {
                this.modeName = string.substring(n + 1, n2);
                this.paddingName = string.substring(n2 + 1);
            }
        }
    }

    private void setNames(String string, String string2, String string3, String string4) {
        if (this.cipherName == null) {
            this.cipherName = string;
        }
        if (this.modeName == null) {
            this.modeName = string2;
        }
        if (this.paddingName == null) {
            this.paddingName = string3;
        }
        if (this.provider == null) {
            this.provider = string4;
        }
    }

    protected final PaddingScheme getPaddingScheme() {
        return this.padding;
    }

    public static Cipher getInstance(String string) throws NoSuchAlgorithmException {
        try {
            return Cipher.getInstance(string, null);
        }
        catch (NoSuchProviderException noSuchProviderException) {
            throw new NoSuchAlgorithmException(noSuchProviderException.getMessage());
        }
    }

    public static Cipher getInstance(String string, String string2) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (string == null) {
            throw new NullPointerException("algorithm == null");
        }
        String string3 = string;
        String string4 = "ECB";
        String string5 = "NONE";
        int n = string.indexOf(47);
        if (n != -1) {
            string3 = string.substring(0, n);
            int n2 = string.indexOf(47, n + 1);
            if (n2 == -1) {
                string4 = string.substring(n + 1);
            } else {
                string4 = string.substring(n + 1, n2);
                string5 = string.substring(n2 + 1);
            }
        }
        return Cipher.getInstance(string3, string4, string5, string2);
    }

    private static Cipher getInstance(String string, String string2, String string3, String string4) throws NoSuchAlgorithmException, NoSuchProviderException {
        Cipher cipher;
        PaddingScheme paddingScheme;
        Cipher cipher2;
        block10: {
            if (debuglevel >= 3) {
                Cipher.debug("Entered getInstance(\"" + string + "\", \"" + string2 + "\", \"" + string3 + "\", \"" + string4 + "\")");
            }
            string = IJCE.getStandardName(string, "Cipher");
            string2 = IJCE.getStandardName(string2, "Mode");
            string3 = IJCE.getStandardName(string3, "PaddingScheme");
            cipher2 = null;
            paddingScheme = null;
            try {
                cipher = (Cipher)IJCE.getImplementation(string + "/" + string2 + "/" + string3, string4, "Cipher");
            }
            catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                if (string2.equals("ECB")) {
                    cipher = (Cipher)IJCE.getImplementation(string, string4, "Cipher");
                } else {
                    try {
                        cipher = (Cipher)IJCE.getImplementation(string + "/" + string2, string4, "Cipher");
                    }
                    catch (NoSuchAlgorithmException noSuchAlgorithmException2) {
                        cipher2 = (Cipher)IJCE.getImplementation(string, string4, "Cipher");
                        cipher2.setNames(string, "ECB", "NONE", string4);
                        cipher = (Cipher)IJCE.getImplementation(string2, string4, "Mode");
                    }
                }
                if (string3.equals("NONE")) break block10;
                paddingScheme = (PaddingScheme)IJCE.getImplementation(string3, string4, "PaddingScheme");
            }
        }
        cipher.setNames(string, string2, string3, string4);
        if (cipher2 != null) {
            ((Mode)cipher).engineSetCipher(cipher2);
        }
        if (paddingScheme != null) {
            cipher.engineSetPaddingScheme(paddingScheme);
        }
        if (debuglevel >= 3) {
            Cipher.debug("Created cipher [1]: " + cipher);
        }
        return cipher;
    }

    public static Cipher getInstance(Cipher cipher, Mode mode, PaddingScheme paddingScheme) {
        Cipher cipher2;
        if (cipher == null) {
            throw new NullPointerException("cipher == null");
        }
        String string = cipher.getAlgorithm();
        String string2 = mode == null ? "ECB" : mode.getAlgorithm();
        String string3 = paddingScheme == null ? "NONE" : paddingScheme.getAlgorithm();
        String string4 = cipher.getProvider();
        Cipher cipher3 = null;
        if (mode == null) {
            cipher2 = cipher;
        } else {
            cipher3 = cipher;
            cipher2 = mode;
        }
        cipher2.setNames(string, string2, string3, string4);
        if (cipher3 != null) {
            ((Mode)cipher2).engineSetCipher(cipher3);
        }
        if (paddingScheme != null) {
            cipher2.engineSetPaddingScheme(paddingScheme);
        }
        if (debuglevel >= 3) {
            Cipher.debug("Created cipher [2]: " + cipher2);
        }
        return cipher2;
    }

    public final int getState() {
        return this.state;
    }

    public final String getAlgorithm() {
        return this.cipherName;
    }

    public final String getMode() {
        return this.modeName == null ? "ECB" : this.modeName;
    }

    public final String getPadding() {
        return this.paddingName == null ? "NONE" : this.paddingName;
    }

    public final String getProvider() {
        return this.provider;
    }

    public final boolean isPaddingBlockCipher() {
        return this.getPlaintextBlockSize() > 1 && this.getPaddingScheme() != null;
    }

    public final int outBufferSize(int n) {
        return this.outBufferSizeInternal(n, false);
    }

    public final int outBufferSizeFinal(int n) {
        return this.outBufferSizeInternal(n, true);
    }

    public final int inBufferSize(int n) {
        return this.inBufferSizeInternal(n, false);
    }

    public final int inBufferSizeFinal(int n) {
        return this.inBufferSizeInternal(n, true);
    }

    public final int blockSize() {
        int n = this.enginePlaintextBlockSize();
        if (n != this.engineCiphertextBlockSize()) {
            throw new IllegalBlockSizeException("blockSize() called when plaintext and ciphertext block sizes differ");
        }
        return n;
    }

    public final int getInputBlockSize() {
        switch (this.getState()) {
            case 1: {
                return this.enginePlaintextBlockSize();
            }
            case 2: {
                return this.engineCiphertextBlockSize();
            }
            default: {
                IJCE.reportBug("invalid Cipher state: " + this.getState());
            }
            case 0: 
        }
        throw new Error("cipher uninitialized");
    }

    public final int getOutputBlockSize() {
        switch (this.getState()) {
            case 1: {
                return this.engineCiphertextBlockSize();
            }
            case 2: {
                return this.enginePlaintextBlockSize();
            }
            default: {
                IJCE.reportBug("invalid Cipher state: " + this.getState());
            }
            case 0: 
        }
        throw new Error("cipher uninitialized");
    }

    public final int getPlaintextBlockSize() {
        return this.enginePlaintextBlockSize();
    }

    public final int getCiphertextBlockSize() {
        return this.engineCiphertextBlockSize();
    }

    public final void initEncrypt(Key key) throws KeyException {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        if (this.tracing) {
            this.traceVoidMethod("engineInitEncrypt(<" + key + ">)");
        }
        this.engineInitEncrypt(key);
        this.state = 1;
        this.inputSize = this.enginePlaintextBlockSize();
        this.outputSize = this.engineCiphertextBlockSize();
        if (this.inputSize < 1 || this.outputSize < 1) {
            this.state = 0;
            throw new Error("input or output block size < 1");
        }
        this.buffer = !this.implBuffering && this.inputSize > 1 ? new byte[this.inputSize] : null;
        this.buffered = 0;
        if (this.padding != null) {
            this.padding.engineSetBlockSize(this.inputSize);
        }
    }

    public final void initDecrypt(Key key) throws KeyException {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        if (this.tracing) {
            this.traceVoidMethod("engineInitDecrypt(<" + key + ">)");
        }
        this.engineInitDecrypt(key);
        this.state = 2;
        this.inputSize = this.engineCiphertextBlockSize();
        this.outputSize = this.enginePlaintextBlockSize();
        if (this.inputSize < 1 || this.outputSize < 1) {
            this.state = 0;
            throw new Error("input or output block size < 1");
        }
        this.buffer = !this.implBuffering && this.inputSize > 1 ? new byte[this.inputSize] : null;
        this.buffered = 0;
        if (this.padding != null) {
            this.padding.engineSetBlockSize(this.outputSize);
        }
    }

    public final byte[] update(byte[] byArray) {
        return this.update(byArray, 0, byArray.length);
    }

    public final byte[] update(byte[] byArray, int n, int n2) {
        byte[] byArray2 = new byte[this.outBufferSizeInternal(n2, false)];
        int n3 = this.updateInternal(byArray, n, n2, byArray2, 0, false);
        if (n3 != byArray2.length) {
            byte[] byArray3 = new byte[n3];
            System.arraycopy(byArray2, 0, byArray3, 0, n3);
            return byArray3;
        }
        return byArray2;
    }

    public final int update(byte[] byArray, int n, int n2, byte[] byArray2) {
        return this.updateInternal(byArray, n, n2, byArray2, 0, false);
    }

    public final int update(byte[] byArray, int n, int n2, byte[] byArray2, int n3) {
        return this.updateInternal(byArray, n, n2, byArray2, n3, false);
    }

    public final byte[] crypt(byte[] byArray) throws IllegalBlockSizeException {
        return this.crypt(byArray, 0, byArray.length);
    }

    public final byte[] crypt(byte[] byArray, int n, int n2) throws IllegalBlockSizeException {
        byte[] byArray2 = new byte[this.outBufferSizeInternal(n2, true)];
        int n3 = this.updateInternal(byArray, n, n2, byArray2, 0, true);
        if (n3 != byArray2.length) {
            byte[] byArray3 = new byte[n3];
            System.arraycopy(byArray2, 0, byArray3, 0, n3);
            return byArray3;
        }
        return byArray2;
    }

    public final int crypt(byte[] byArray, int n, int n2, byte[] byArray2, int n3) throws IllegalBlockSizeException {
        return this.updateInternal(byArray, n, n2, byArray2, n3, true);
    }

    public final byte[] doFinal(byte[] byArray) throws IllegalBlockSizeException {
        return this.crypt(byArray, 0, byArray.length);
    }

    public final byte[] doFinal(byte[] byArray, int n, int n2) throws IllegalBlockSizeException {
        return this.crypt(byArray, n, n2);
    }

    public final int doFinal(byte[] byArray, int n, int n2, byte[] byArray2) throws IllegalBlockSizeException {
        return this.crypt(byArray, n, n2, byArray2, 0);
    }

    public final int doFinal(byte[] byArray, int n, int n2, byte[] byArray2, int n3) throws IllegalBlockSizeException {
        return this.crypt(byArray, n, n2, byArray2, n3);
    }

    private int outBufferSizeInternal(int n, boolean bl) {
        int n2;
        if (n < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        if (!this.implBuffering) {
            n2 = (n += this.buffered) % this.inputSize;
            n -= n2;
            if (bl && this.state == 1 && (this.padding != null || n2 > 0)) {
                n += this.inputSize;
            }
        }
        if (n < 0) {
            IJCE.reportBug("inLen < 0");
        }
        if (this.tracing) {
            this.traceMethod("engineOutBufferSize(" + n + ", " + bl + ")");
        }
        n2 = this.engineOutBufferSize(n, bl);
        if (this.tracing) {
            this.traceResult(n2);
        }
        return n2;
    }

    private int inBufferSizeInternal(int n, boolean bl) {
        int n2;
        if (!this.implBuffering && (n2 = n % this.outputSize) > 0) {
            n += this.outputSize - n2;
        }
        if (this.tracing) {
            this.traceMethod("engineInBufferSize(" + n + ", " + bl + ")");
        }
        n2 = this.engineInBufferSize(n, bl);
        if (this.tracing) {
            this.traceResult(n2);
        }
        if (!this.implBuffering) {
            if (bl && this.state == 1 && this.padding != null) {
                n2 -= this.inputSize;
            }
            n2 -= this.buffered;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        return n2;
    }

    private int updateInternal(byte[] byArray, int n, int n2, byte[] byArray2, int n3, boolean bl) {
        if (debuglevel >= 5 && this.tracing) {
            this.traceMethod("updateInternal(<" + Cipher.dump(byArray) + ">, " + n + ", " + n2 + ", <" + Cipher.dump(byArray2) + ">, " + n3 + ", " + bl + ")");
        }
        boolean bl2 = false;
        int n4 = n3;
        try {
            byte[] byArray3;
            if (this.state == 0) {
                throw new IllegalStateException("cipher uninitialized");
            }
            if (n2 < 0) {
                throw new IllegalArgumentException("inLen < 0");
            }
            if (n < 0 || n3 < 0 || (long)n + (long)n2 > (long)byArray.length) {
                if (debuglevel >= 1) {
                    Cipher.debug("inOffset = " + n + ", inLen = " + n2 + ", outOffset = " + n3 + ", in.length = " + byArray.length);
                }
                throw new ArrayIndexOutOfBoundsException("inOffset < 0  || outOffset < 0 || (long)inOffset+inLen > in.length");
            }
            if (byArray2 == null) {
                throw new NullPointerException();
            }
            if (this.buffer == null) {
                int n5;
                if (this.tracing) {
                    this.traceMethod("engineUpdate(<" + Cipher.dump(byArray) + ">, " + n + ", " + n2 + ", <" + Cipher.dump(byArray2) + ">, " + n3 + ")");
                    n5 = this.engineUpdate(byArray, n, n2, byArray2, n3);
                    this.traceResult(n5);
                    n3 += n5;
                    if (bl && this.implBuffering) {
                        this.traceMethod("engineCrypt(<" + Cipher.dump(byArray2) + ">, " + n3 + ")");
                        n5 = this.engineCrypt(byArray2, n3);
                        this.traceResult(n5);
                        n3 += n5;
                    }
                } else {
                    n3 += this.engineUpdate(byArray, n, n2, byArray2, n3);
                    if (bl && this.implBuffering) {
                        n3 += this.engineCrypt(byArray2, n3);
                    }
                }
                n5 = n3 - n4;
                return n5;
            }
            if (byArray == byArray2 && (n3 >= n && (long)n3 < (long)n + (long)n2 || n >= n3 && (long)n < (long)n3 + (long)this.outBufferSizeInternal(n2, bl))) {
                byArray3 = new byte[n2];
                System.arraycopy(byArray, n, byArray3, 0, n2);
                byArray = byArray3;
                n = 0;
            }
            if (bl) {
                if (this.state == 1) {
                    n3 += this.updateInternal(byArray, n, n2, byArray2, n3, false);
                    if (this.padding == null) {
                        if (this.buffered > 0) {
                            this.buffered = 0;
                            throw new IllegalBlockSizeException(this.getAlgorithm() + ": Non-padding cipher in ENCRYPT state with an incomplete final block");
                        }
                        int n6 = n3 - n4;
                        return n6;
                    }
                    this.padding.pad(this.buffer, 0, this.buffered);
                    this.buffered = 0;
                    if (this.tracing) {
                        this.traceMethod("engineUpdate(<" + Cipher.dump(this.buffer) + ">, 0, " + this.inputSize + ", <" + Cipher.dump(byArray2) + ">, " + n3 + ")");
                    }
                    int n7 = this.engineUpdate(this.buffer, 0, this.inputSize, byArray2, n3);
                    if (this.tracing) {
                        this.traceResult(n7);
                    }
                    int n8 = (n3 += n7) - n4;
                    return n8;
                }
                if (this.padding != null) {
                    if (n2 == 0) {
                        int n9 = 0;
                        return n9;
                    }
                    n3 += this.updateInternal(byArray, n, n2 - 1, byArray2, n3, false);
                    if (this.buffered != this.inputSize - 1) {
                        this.buffered = 0;
                        throw new IllegalBlockSizeException(this.getAlgorithm() + ": Cipher in DECRYPT state with an incomplete final block");
                    }
                    this.buffer[this.buffered] = byArray[n + n2 - 1];
                    this.buffered = 0;
                    byArray3 = new byte[this.outBufferSizeInternal(this.inputSize, false)];
                    if (this.tracing) {
                        this.traceMethod("engineUpdate(<" + Cipher.dump(this.buffer) + ">, 0, " + this.inputSize + ", <" + Cipher.dump(byArray3) + ">, 0)");
                    }
                    int n10 = this.engineUpdate(this.buffer, 0, this.inputSize, byArray3, 0);
                    if (this.tracing) {
                        this.traceResult(n10);
                    }
                    int n11 = this.padding.unpad(byArray3, 0, byArray3.length);
                    System.arraycopy(byArray3, 0, byArray2, n3, n11);
                    int n12 = (n3 += n11) - n4;
                    return n12;
                }
            }
            if (this.buffered > 0) {
                if ((long)n2 + (long)this.buffered < (long)this.inputSize) {
                    System.arraycopy(byArray, n, this.buffer, this.buffered, n2);
                    this.buffered += n2;
                    int n13 = 0;
                    return n13;
                }
                int n14 = this.inputSize - this.buffered;
                System.arraycopy(byArray, n, this.buffer, this.buffered, n14);
                n += n14;
                n2 -= n14;
                if (this.tracing) {
                    this.traceMethod("engineUpdate(<" + Cipher.dump(this.buffer) + ">, 0, " + this.inputSize + ", <" + Cipher.dump(byArray2) + ">, " + n3 + ")");
                }
                int n15 = this.engineUpdate(this.buffer, 0, this.inputSize, byArray2, n3);
                if (this.tracing) {
                    this.traceResult(n15);
                }
                n3 += n15;
            }
            this.buffered = n2 % this.inputSize;
            if (this.buffered > 0) {
                System.arraycopy(byArray, n + n2 - this.buffered, this.buffer, 0, this.buffered);
                n2 -= this.buffered;
            }
            while (n2 > 0) {
                if (this.tracing) {
                    this.traceMethod("engineUpdate(<" + Cipher.dump(byArray) + ">, " + n + ", " + this.inputSize + ", <" + Cipher.dump(byArray2) + ">, " + n3 + ")");
                }
                int n16 = this.engineUpdate(byArray, n, this.inputSize, byArray2, n3);
                if (this.tracing) {
                    this.traceResult(n16);
                }
                n3 += n16;
                n += this.inputSize;
                n2 -= this.inputSize;
            }
            int n17 = n3 - n4;
            return n17;
        }
        catch (RuntimeException runtimeException) {
            if (this.tracing) {
                runtimeException.printStackTrace();
            }
            bl2 = true;
            throw runtimeException;
        }
        finally {
            if (debuglevel >= 5 && this.tracing && !bl2) {
                this.traceResult(n3 - n4);
            }
        }
    }

    public void setParameter(String string, Object object) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        if (string == null) {
            throw new NullPointerException("param == null");
        }
        if (this.tracing) {
            this.traceVoidMethod("engineSetParameter(\"" + string + "\", <" + object + ">)");
        }
        this.engineSetParameter(string, object);
    }

    public Object getParameter(String string) throws NoSuchParameterException, InvalidParameterException {
        if (string == null) {
            throw new NullPointerException("param == null");
        }
        if (this.tracing) {
            this.traceMethod("engineGetParameter(\"" + string + "\")");
        }
        Object object = this.engineGetParameter(string);
        if (this.tracing) {
            this.traceResult("<" + object + ">");
        }
        return object;
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    public String toString() {
        return "Cipher [" + this.getProvider() + " " + this.getAlgorithm() + "/" + this.getMode() + "/" + this.getPadding() + "]";
    }

    protected void engineSetPaddingScheme(PaddingScheme paddingScheme) {
        if (this.state != 0) {
            throw new IllegalStateException("Cipher is already initialized");
        }
        this.padding = paddingScheme;
    }

    protected int engineBlockSize() {
        throw new Error("cipher classes must implement either engineBlockSize, or enginePlaintextBlockSize and engineCiphertextBlockSize");
    }

    protected int enginePlaintextBlockSize() {
        return this.engineBlockSize();
    }

    protected int engineCiphertextBlockSize() {
        return this.engineBlockSize();
    }

    protected int engineOutBufferSize(int n, boolean bl) {
        return n / this.inputSize * this.outputSize;
    }

    protected int engineInBufferSize(int n, boolean bl) {
        return n / this.outputSize * this.inputSize;
    }

    protected abstract void engineInitEncrypt(Key var1) throws KeyException;

    protected abstract void engineInitDecrypt(Key var1) throws KeyException;

    protected abstract int engineUpdate(byte[] var1, int var2, int var3, byte[] var4, int var5);

    protected int engineCrypt(byte[] byArray, int n) {
        return 0;
    }

    protected void engineSetParameter(String string, Object object) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        throw new NoSuchParameterException(this.getAlgorithm() + ": " + string);
    }

    protected Object engineGetParameter(String string) throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(this.getAlgorithm() + ": " + string);
    }

    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "Cipher");
    }

    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("Cipher");
    }
}

