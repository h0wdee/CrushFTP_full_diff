/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh1;

import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.ssh.components.jce.Ssh2RsaPrivateCrtKey;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public final class Rsa {
    private static BigInteger ONE = BigInteger.valueOf(1L);

    public static BigInteger doPrivateCrt(BigInteger input, BigInteger privateExponent, BigInteger primeP, BigInteger primeQ, BigInteger crtCoefficient) {
        return Rsa.doPrivateCrt(input, primeP, primeQ, Rsa.getPrimeExponent(privateExponent, primeP), Rsa.getPrimeExponent(privateExponent, primeQ), crtCoefficient);
    }

    public static BigInteger doPrivateCrt(BigInteger input, BigInteger p, BigInteger q, BigInteger dP, BigInteger dQ, BigInteger qInv) {
        if (!qInv.equals(q.modInverse(p))) {
            BigInteger t = p;
            p = q;
            q = t;
            t = dP;
            dP = dQ;
            dQ = t;
        }
        BigInteger s_1 = input.modPow(dP, p);
        BigInteger s_2 = input.modPow(dQ, q);
        BigInteger h = qInv.multiply(s_1.subtract(s_2)).mod(p);
        return s_2.add(h.multiply(q));
    }

    public static BigInteger getPrimeExponent(BigInteger privateExponent, BigInteger prime) {
        BigInteger pe = prime.subtract(ONE);
        return privateExponent.mod(pe);
    }

    public static BigInteger padPKCS1(BigInteger input, int type, int padLen) throws IllegalStateException {
        int inByteLen = (input.bitLength() + 7) / 8;
        if (inByteLen > padLen - 11) {
            throw new IllegalStateException("PKCS1 failed to pad input! input=" + String.valueOf(inByteLen) + " padding=" + String.valueOf(padLen));
        }
        byte[] padBytes = new byte[padLen - inByteLen - 3 + 1];
        padBytes[0] = 0;
        SecureRandom rnd = JCEComponentManager.getSecureRandom();
        for (int i = 1; i < padLen - inByteLen - 3 + 1; ++i) {
            if (type == 1) {
                padBytes[i] = -1;
                continue;
            }
            byte[] b = new byte[1];
            do {
                rnd.nextBytes(b);
            } while (b[0] == 0);
            padBytes[i] = b[0];
        }
        BigInteger rndInt = new BigInteger(1, padBytes);
        rndInt = rndInt.shiftLeft((inByteLen + 1) * 8);
        BigInteger result = BigInteger.valueOf(type);
        result = result.shiftLeft((padLen - 2) * 8);
        result = result.or(rndInt);
        result = result.or(input);
        return result;
    }

    public static BigInteger removePKCS1(BigInteger input, int type) throws IllegalStateException {
        int i;
        byte[] strip = input.toByteArray();
        if (strip[0] != type) {
            throw new IllegalStateException("PKCS1 padding type " + type + " is not valid");
        }
        for (i = 1; i < strip.length && strip[i] != 0; ++i) {
            if (type != 1 || strip[i] == -1) continue;
            throw new IllegalStateException("Corrupt data found in expected PKSC1 padding");
        }
        if (i == strip.length) {
            throw new IllegalStateException("Corrupt data found in expected PKSC1 padding");
        }
        byte[] val = new byte[strip.length - i];
        System.arraycopy(strip, i, val, 0, val.length);
        return new BigInteger(1, val);
    }

    public static Ssh2RsaPrivateCrtKey generateKey(int bits, SecureRandom secRand) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return Rsa.generateKey(bits, BigInteger.valueOf(65537L), secRand);
    }

    public static Ssh2RsaPrivateCrtKey generateKey(int bits, BigInteger e, SecureRandom secRand) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger p = null;
        BigInteger q = null;
        BigInteger t = null;
        BigInteger phi = null;
        BigInteger d = null;
        BigInteger u = null;
        BigInteger n = null;
        boolean finished = false;
        BigInteger ONE = BigInteger.valueOf(1L);
        int pbits = (bits + 1) / 2;
        int qbits = bits - pbits;
        while (!finished) {
            p = new BigInteger(pbits, 80, secRand);
            q = new BigInteger(qbits, 80, secRand);
            if (p.compareTo(q) == 0) continue;
            if (p.compareTo(q) < 0) {
                t = q;
                q = p;
                p = t;
            }
            if (!p.isProbablePrime(25) || !q.isProbablePrime(25) || (t = p.gcd(q)).compareTo(ONE) != 0 || (n = p.multiply(q)).bitLength() != bits) continue;
            phi = p.subtract(ONE).multiply(q.subtract(ONE));
            d = e.modInverse(phi);
            u = q.modInverse(p);
            finished = true;
        }
        return new Ssh2RsaPrivateCrtKey(n, e, d, p, q, Rsa.getPrimeExponent(d, p), Rsa.getPrimeExponent(d, q), u);
    }

    public static BigInteger doPublic(BigInteger input, BigInteger modulus, BigInteger publicExponent) {
        return input.modPow(publicExponent, modulus);
    }

    public static BigInteger doPrivate(BigInteger input, BigInteger modulus, BigInteger privateExponent) {
        return Rsa.doPublic(input, modulus, privateExponent);
    }
}

