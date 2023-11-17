/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.elgamal.BaseElGamalKeyPairGenerator;
import com.didisoft.pgp.bc.elgamal.CryptixException;
import com.didisoft.pgp.bc.elgamal.ElGamalAlgorithm;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalPrivateKey;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalPublicKey;
import com.didisoft.pgp.bc.elgamal.security.AsymmetricCipher;
import com.didisoft.pgp.bc.elgamal.security.Cipher;
import com.didisoft.pgp.bc.elgamal.security.IllegalBlockSizeException;
import com.didisoft.pgp.bc.elgamal.util.ArrayUtil;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Random;

public class RawElGamalCipher
extends Cipher
implements AsymmetricCipher,
Cloneable {
    private static final int POSITIVE = 1;
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private BigInteger p;
    private BigInteger p_minus_1;
    private BigInteger g;
    private BigInteger x;
    private BigInteger y;
    private int primeLen;
    private Random rng;

    public RawElGamalCipher() {
        super(false, true, "Cryptix");
    }

    protected void engineInitEncrypt(Key key) throws KeyException {
        if (!(key instanceof ElGamalPublicKey)) {
            throw new InvalidKeyException("ElGamal: encryption key does not implement java.security.interfaces.ElGamalPublicKey");
        }
        ElGamalPublicKey elGamalPublicKey = (ElGamalPublicKey)key;
        this.initInternal(elGamalPublicKey.getP(), elGamalPublicKey.getG(), null, elGamalPublicKey.getY());
        if (this.rng == null) {
            this.rng = IOUtil.getSecureRandom();
        }
    }

    protected void engineInitDecrypt(Key key) throws KeyException {
        if (!(key instanceof ElGamalPrivateKey)) {
            throw new InvalidKeyException("ElGamal: decryption key does not implement java.security.interfaces.ElGamalPrivateKey");
        }
        ElGamalPrivateKey elGamalPrivateKey = (ElGamalPrivateKey)key;
        BigInteger bigInteger = elGamalPrivateKey.getX();
        if (bigInteger == null) {
            throw new InvalidKeyException("ElGamal: getX() == null");
        }
        this.initInternal(elGamalPrivateKey.getP(), elGamalPrivateKey.getG(), bigInteger, elGamalPrivateKey.getY());
    }

    private void initInternal(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) throws InvalidKeyException {
        if (bigInteger == null) {
            throw new InvalidKeyException("ElGamal: getP() == null");
        }
        if (bigInteger2 == null) {
            throw new InvalidKeyException("ElGamal: getG() == null");
        }
        if (bigInteger4 == null) {
            throw new InvalidKeyException("ElGamal: getY() == null");
        }
        this.p = bigInteger;
        this.g = bigInteger2;
        this.x = bigInteger3;
        this.y = bigInteger4;
        this.primeLen = (this.p.bitLength() - 1) / 8;
    }

    protected int enginePlaintextBlockSize() {
        if (this.primeLen == 0) {
            throw new CryptixException("ElGamal: plaintext block size is not valid until key is set");
        }
        return this.primeLen;
    }

    protected int engineCiphertextBlockSize() {
        if (this.primeLen == 0) {
            throw new CryptixException("ElGamal: ciphertext block size is not valid until key is set");
        }
        return this.primeLen * 2;
    }

    protected void engineSetParameter(String string, Object object) {
        if (string.equals("random")) {
            if (!(object instanceof Random)) {
                throw new InvalidParameterException("value must be an instance of java.util.Random");
            }
            this.rng = (Random)object;
            return;
        }
        throw new InvalidParameterException(string);
    }

    protected Object engineGetParameter(String string) {
        if (string.equals("random")) {
            return this.rng;
        }
        return null;
    }

    protected int engineUpdate(byte[] byArray, int n, int n2, byte[] byArray2, int n3) {
        if (n2 <= 0) {
            return 0;
        }
        if (this.getState() == 1) {
            if (n2 != this.primeLen) {
                throw new IllegalBlockSizeException("inLen = " + n2 + ", plaintext block size = " + this.primeLen);
            }
            byte[] byArray3 = new byte[this.primeLen];
            System.arraycopy(byArray, n, byArray3, 0, this.primeLen);
            BigInteger[] bigIntegerArray = new BigInteger[2];
            BigInteger bigInteger = new BigInteger(1, byArray3);
            ElGamalAlgorithm.encrypt(bigInteger, bigIntegerArray, this.p, this.g, this.y, this.rng);
            byte[] byArray4 = bigIntegerArray[0].toByteArray();
            byte[] byArray5 = bigIntegerArray[1].toByteArray();
            ArrayUtil.clear(byArray2, n3, this.primeLen * 2);
            System.arraycopy(byArray4, 0, byArray2, n3 + this.primeLen - byArray4.length, byArray4.length);
            System.arraycopy(byArray5, 0, byArray2, n3 + this.primeLen * 2 - byArray5.length, byArray5.length);
            ArrayUtil.clear(byArray3);
            return this.primeLen * 2;
        }
        if (n2 != this.primeLen * 2) {
            throw new IllegalBlockSizeException("inLen = " + n2 + ", ciphertext block size = " + this.primeLen * 2);
        }
        byte[] byArray6 = new byte[this.primeLen];
        System.arraycopy(byArray, n, byArray6, 0, this.primeLen);
        BigInteger bigInteger = new BigInteger(1, byArray6);
        System.arraycopy(byArray, n + this.primeLen, byArray6, 0, this.primeLen);
        BigInteger bigInteger2 = new BigInteger(1, byArray6);
        BigInteger bigInteger3 = ElGamalAlgorithm.decrypt(bigInteger, bigInteger2, this.p, this.g, this.x);
        byte[] byArray7 = bigInteger3.toByteArray();
        ArrayUtil.clear(byArray2, n3, this.primeLen - byArray7.length);
        System.arraycopy(byArray7, 0, byArray2, n3 + this.primeLen - byArray7.length, byArray7.length);
        return this.primeLen;
    }

    public static final void main(String[] stringArray) {
        try {
            RawElGamalCipher.self_test(new PrintWriter(System.out, true));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void self_test(PrintWriter printWriter) throws KeyException {
        BaseElGamalKeyPairGenerator baseElGamalKeyPairGenerator = new BaseElGamalKeyPairGenerator();
        SecureRandom secureRandom = IOUtil.getSecureRandom();
        long l = System.currentTimeMillis();
        ((KeyPairGenerator)baseElGamalKeyPairGenerator).initialize(385, secureRandom);
        KeyPair keyPair = ((KeyPairGenerator)baseElGamalKeyPairGenerator).generateKeyPair();
        long l2 = System.currentTimeMillis() - l;
        printWriter.println("Keygen: " + (float)l2 / 1000.0f + " seconds");
        RawElGamalCipher rawElGamalCipher = new RawElGamalCipher();
        rawElGamalCipher.test(printWriter, keyPair, secureRandom);
    }

    private void test(PrintWriter printWriter, KeyPair keyPair, SecureRandom secureRandom) throws KeyException {
        ElGamalPrivateKey elGamalPrivateKey = (ElGamalPrivateKey)keyPair.getPrivate();
        ElGamalPublicKey elGamalPublicKey = (ElGamalPublicKey)keyPair.getPublic();
        BigInteger bigInteger = new BigInteger(elGamalPrivateKey.getP().bitLength() - 1, secureRandom);
        this.rng = secureRandom;
        long l = System.currentTimeMillis();
        this.initEncrypt(elGamalPublicKey);
        BigInteger[] bigIntegerArray = new BigInteger[2];
        ElGamalAlgorithm.encrypt(bigInteger, bigIntegerArray, this.p, this.g, this.y, this.rng);
        long l2 = System.currentTimeMillis();
        this.initDecrypt(elGamalPrivateKey);
        BigInteger bigInteger2 = ElGamalAlgorithm.decrypt(bigIntegerArray[0], bigIntegerArray[1], this.p, this.g, this.x);
        long l3 = System.currentTimeMillis();
        printWriter.println("p = " + this.p);
        printWriter.println("g = " + this.g);
        printWriter.println("x = " + this.x);
        printWriter.println("y = " + this.y);
        printWriter.println("M = " + bigInteger);
        printWriter.println("a = " + bigIntegerArray[0]);
        printWriter.println("b = " + bigIntegerArray[1]);
        if (!bigInteger.equals(bigInteger2)) {
            printWriter.println("DECRYPTION FAILED!");
            printWriter.println("M' = " + bigInteger2);
        }
        printWriter.println("Encrypt: " + (float)(l2 - l) / 1000.0f + " seconds");
        printWriter.println("Decrypt: " + (float)(l3 - l2) / 1000.0f + " seconds");
    }
}

